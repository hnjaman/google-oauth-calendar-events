package com.timetackle.hometask.googleoauthcalendarevents.controller;

import com.timetackle.hometask.googleoauthcalendarevents.CalendarQuickstart;
import com.timetackle.hometask.googleoauthcalendarevents.dto.EventDto;
import com.timetackle.hometask.googleoauthcalendarevents.dto.EventTransferDto;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class Controller {

	@GetMapping("/")
	public String login(){
		return "<!DOCTYPE html>\n" +
				"<html>\n" +
				"   <head>\n" +
				"      <meta charset = \"ISO-8859-1\">\n" +
				"      <title>Login</title>\n" +
				"   </head>\n" +
				"   <body style='margin-left: 300px;'>\n" +
				"      <a href = \"https://accounts.google.com/o/oauth2/auth/oauthchooseaccount?access_type=online&client_id=926678020312-j5gulbmeccqdhv2v9aniip9use4nv3tj.apps.googleusercontent.com&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2FCallback&response_type=code&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fcalendar.readonly&flowName=GeneralOAuthFlow\">Login by Google</a>\n" +
				"   </body>\n" +
				"</html> ";
	}

	@GetMapping("/agenda")
	public String getAgenda(@RequestParam String code) {
		EventTransferDto eventTransferDto = null;
		try{
			CalendarQuickstart calendarQuickstart = new CalendarQuickstart();
			eventTransferDto = calendarQuickstart.getEvents(code);
		} catch (Exception e){
			e.fillInStackTrace();
		}

		String html = "<!DOCTYPE html>\n" +
				"		<html>\n" +
				"   		<head>\n" +
				"      			<meta charset = \"ISO-8859-1\">\n" +
				"      			<title>Welcome</title>\n" +
				"				<style>\n" +
				"					table, th, td {\n" +
				"  						border: 1px solid black;\n" +
				"  						border-collapse: collapse;\n" +
				"					}\n" +
				"				</style>" +
				"   		</head>\n" +
				"   		<body style='margin-left: 300px;'>\n" +
				"      			<h5><a href=\"/\">Go Home</a></h5>" +
				"      			<h3>Today's agenda</h3>" +
				"				<table>\n" +
				"  					<tr>\n" +
				"    					<th>Time</th>\n" +
				"    					<th>Event</th>\n" +
				"  					</tr>";

		if(eventTransferDto != null) {
			for (EventDto event : eventTransferDto.getEventDtoList()) {
				html += "<tr>\n" +
						"    <td>"+event.getStart()+"-"+event.getEnd()+"</td>\n" +
						"    <td>"+event.getDescription()+"</td>\n" +
						"</tr>";
			}
			html += "</table>";

			html += "<h3>Today's available slots (At least 1 hour)</h3>";

			html += "<table>" +
					"	<tr>" +
					"		<th>Time</th>" +
					"		<th>Event</th>" +
					"	</tr>";
			for (Map.Entry<Integer, Boolean> slot : eventTransferDto.getSlots().entrySet()){
				if (!slot.getValue()) {
					html +=	"<tr>\n" +
							"    <td>"+slot.getKey()+"-"+(slot.getKey()+1)+"</td>\n" +
							"    <td><b>Free</b></td>\n" +
							"</tr>";
				}
			}
		}

		html += "   	</table>" +
				"	</body>\n" +
				"</html> ";

		return html;
	}

	@GetMapping("/Callback")
	public String callback(@RequestParam String code){
		System.out.println(code);
		return "<!DOCTYPE html>\n" +
				"<html>\n" +
				"   <head>\n" +
				"      <meta charset = \"ISO-8859-1\">\n" +
				"      <title>Welcome</title>\n" +
				"   </head>\n" +
				"   <body style='margin-left: 300px;'>\n" +
				"      <a href = \"/agenda?code="+code+"\">View Today's Agenda</a>\n" +
				"   </body>\n" +
				"</html> ";
	}
}