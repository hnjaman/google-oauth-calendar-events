package com.timetackle.hometask.googleoauthcalendarevents;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.timetackle.hometask.googleoauthcalendarevents.dto.EventDto;
import com.timetackle.hometask.googleoauthcalendarevents.dto.EventTransferDto;
import org.springframework.stereotype.Service;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;


@Service
public class CalendarQuickstart {
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/google-client-secret.json";

    private EventDto populateScheduleMap(long start, long end, Map<Integer, Boolean> map, String description){
        EventDto eventDto = new EventDto();
        String startDate = new DateTime(start).toString();
        String[] startDateArray = startDate.split("T");
        String startTime = startDateArray[1].substring(0,8);
        String[] startHours = startTime.split(":");
        int startHour = Integer.parseInt(startHours[0]);

        String endDate = new DateTime(end).toString();
        String[] endDateArray = endDate.split("T");
        String endTime = endDateArray[1].substring(0,8);
        String[] endHours = endTime.split(":");
        int endHour = Integer.parseInt(endHours[0]);
        int endHalfHour = Integer.parseInt(endHours[1]);

        int diff = endHour - startHour;
        if(diff == 0){
            map.put(startHour, true);
        } else {
            for(int i=0; i<diff; i++){
                map.put(startHour+i, true);
            }
            if(endHalfHour>0){
                map.put(endHour, true);
            }
        }

        eventDto.setStart(startHours[0]+":"+startHours[1]);
        eventDto.setEnd(endHours[0]+":"+endHours[1]);
        eventDto.setDescription(description);
        return eventDto;
    }

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, String code) throws IOException {
//         Load client secrets.
        InputStream in = CalendarQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

//         Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("online")
                .build();

            TokenResponse response = flow.newTokenRequest(code).setRedirectUri("http://localhost:8080/Callback").execute();
//             store credential and return it
            return flow.createAndStoreCredential(response, "user");
    }


    public EventTransferDto getEvents(String code) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, code))
                .setApplicationName(APPLICATION_NAME)
                .build();

        String dayString = new DateTime(System.currentTimeMillis()).toString();
        String[] dayArray = dayString.split("T");
        String timeZone = dayString.substring(dayString.length() - 6);
        String minCurrentDay = dayArray[0]+"T00:00:01"+timeZone;
        String maxCurrentDay = dayArray[0]+"T23:59:59"+timeZone;

        DateTime todayMin = new DateTime(minCurrentDay);
        DateTime todayMax = new DateTime(maxCurrentDay);

        /**
        * considered we are looking just 1 hour available slot
        * */
        Map<Integer, Boolean> slots = new HashMap<>();
        for(int i=0; i<=23; i++) {
            slots.put(i, false);
        }

        List<EventDto> eventDtoList = new ArrayList<>();
        EventTransferDto eventTransferDto = new EventTransferDto();

        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(todayMin)
                .setTimeMax(todayMax)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setPrettyPrint(true)
                .execute();

        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                DateTime end = event.getEnd().getDateTime();
                eventDtoList.add(populateScheduleMap(start.getValue(), end.getValue(), slots, event.getSummary()));
            }
        }

        eventTransferDto.setEventDtoList(eventDtoList);
        eventTransferDto.setSlots(slots);
        return eventTransferDto;
    }
}