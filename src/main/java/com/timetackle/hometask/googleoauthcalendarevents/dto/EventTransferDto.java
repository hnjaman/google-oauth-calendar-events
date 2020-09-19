package com.timetackle.hometask.googleoauthcalendarevents.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EventTransferDto {
    private Map<Integer, Boolean> slots;
    private List<EventDto> eventDtoList;
}
