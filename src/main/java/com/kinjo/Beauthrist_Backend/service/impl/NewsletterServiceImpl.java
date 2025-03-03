package com.kinjo.Beauthrist_Backend.service.impl;

import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.entity.NewsletterSubscriber;
import com.kinjo.Beauthrist_Backend.entity.Product;
import com.kinjo.Beauthrist_Backend.exception.NotFoundException;
import com.kinjo.Beauthrist_Backend.exception.UserAlreadyExistsException;
import com.kinjo.Beauthrist_Backend.mapper.EntityDtoMapper;
import com.kinjo.Beauthrist_Backend.repository.NewsletterSubscriberRepo;
import com.kinjo.Beauthrist_Backend.repository.ProductRepo;
import com.kinjo.Beauthrist_Backend.service.interf.NewsletterService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context; // Import this
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsletterServiceImpl implements NewsletterService {
    private static final Logger logger = LoggerFactory.getLogger(NewsletterServiceImpl.class);

    private final NewsletterSubscriberRepo subscriberRepo;
    private final JavaMailSender javaMailSender;
    private final EntityDtoMapper entityDtoMapper;
    private final ProductRepo productRepo;
    private final TemplateEngine templateEngine;


    @Override
    public Response subscribe(String email) {
        Optional<NewsletterSubscriber> optionalSubscriber = subscriberRepo.findByEmail(email);

        if (optionalSubscriber.isPresent()) {
            NewsletterSubscriber subscriber = optionalSubscriber.get();

            if (subscriber.getIsActive()) {
                throw new UserAlreadyExistsException("Email already subscribed.");
            }

            // Resubscribe the user
            subscriber.setIsActive(true);
            subscriberRepo.save(subscriber);
            sendConfirmationEmail(email);
            return Response.builder()
                    .status(200)
                    .message("Resubscribed successfully.")
                    .build();
        }

        // Create a new subscriber
        NewsletterSubscriber newSubscriber = new NewsletterSubscriber();
        newSubscriber.setEmail(email);
        newSubscriber.setIsActive(true);
        subscriberRepo.save(newSubscriber);
        sendConfirmationEmail(email);

        return Response.builder()
                .status(200)
                .message("Subscribed successfully.")
                .build();
    }


    @Override
    public Response unsubscribe(String email) {
        NewsletterSubscriber subscriber = subscriberRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email not found."));
        subscriber.setIsActive(false);
        subscriberRepo.save(subscriber);
        return Response.builder()
                .status(200)
                .message("Unsubscribed successfully.")
                .build();
    }

    @Override
    public Response getAllSubscribers() {
        List<NewsletterSubscriber> subscribers = subscriberRepo.findAll().stream()
                .filter(NewsletterSubscriber::getIsActive)
                .collect(Collectors.toList());
        return Response.builder()
                .status(200)
                .subscribers(entityDtoMapper.mapSubscribeToDtoList(subscribers))
                .build();
    }

    //@Scheduled(cron = "0 0 12 * * ?", zone = "Africa/Lagos")
    @Scheduled(cron = "0 */2 * * * ?", zone = "Africa/Lagos")
    @Override
    public void sendDailyNewsletter() {
        List<Product> trendingProducts = productRepo.findTop3ByOrderByLikesDesc();
        List<Product> mostViewedProducts = productRepo.findTop3ByOrderByViewCountDesc();


        List<NewsletterSubscriber> activeSubscribers = subscriberRepo.findByIsActiveTrue();

        activeSubscribers.forEach(subscriber -> {
            try {
                sendNewsletterEmail(subscriber.getEmail(), trendingProducts, mostViewedProducts);

            } catch (Exception e) {
            }
        });
    }

    private void sendNewsletterEmail(String email, List<Product> trendingProducts, List<Product> mostViewedProducts) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Your Daily Newsletter from kinjomarket - " + LocalDateTime.now());
            helper.setFrom("no-reply@kinjomarket.com");

            // Render Thymeleaf template
            Context context = new Context();
            context.setVariable("trendingProducts", trendingProducts);
            context.setVariable("mostViewedProducts", mostViewedProducts);
            String emailContent = templateEngine.process("newsletter", context);

            helper.setText(emailContent, true); // Set HTML content

            javaMailSender.send(message);
        } catch (MessagingException e) {
        }
    }


    private void sendConfirmationEmail(String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Welcome to Our Newsletter!");
        message.setText("Thank you for subscribing to our newsletter! You will receive daily updates on trending products and new arrivals.");
        message.setFrom("no-reply@kinjomarket.com");
        javaMailSender.send(message);
    }
}