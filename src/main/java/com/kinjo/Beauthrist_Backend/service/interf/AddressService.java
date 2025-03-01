package com.kinjo.Beauthrist_Backend.service.interf;

import com.kinjo.Beauthrist_Backend.dto.AddressDto;
import com.kinjo.Beauthrist_Backend.dto.Response;

public interface AddressService {
    Response saveAndUpdateAddress(AddressDto addressDto);

}
