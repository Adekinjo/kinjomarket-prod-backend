package com.kinjo.Beauthrist_Backend.service.interf;

import com.kinjo.Beauthrist_Backend.dto.NewsletterSubscriberDto;
import com.kinjo.Beauthrist_Backend.dto.Response;

import java.util.List;

public interface NewsletterService {
    Response subscribe(String email);
    Response unsubscribe(String email);
    Response getAllSubscribers();
    void sendDailyNewsletter();
}

