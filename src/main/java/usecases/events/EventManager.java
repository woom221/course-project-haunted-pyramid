package usecases.events;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.time.LocalDate;

import entities.Event;
import entities.recursions.RecursiveEvent;
import interfaces.EventListObserver;

/**
 * stores and Manages events
 * edits Events by dictionary key (unique ID)
 * updates EventListObservers when Event is added to its eventMap, removed from its eventMap, or
 * the start or end time of an <code>Event</code> within its <code>eventMap</code> is modified
 *
 * @author Sebin Im
 * @author Taite Cullen
 * @author Malik Lahlou
 * @author Seo Won Yi
 */

public class EventManager {
    private final Map<UUID, Event> eventMap;
    private final RepeatedEventManager repeatedEventManager;
    private EventListObserver[] toUpdate;
    // Get rid of eventMap later when sean fixes Event ID
    private Map<UUID, List<Event>> uuidEventsMap;

    /**
     * constructs event manager. stores list of events by key: ID, value: event in <code>this.eventMap</code>
     * sets <code>this.toUpdate</code> to empty list of <code>EventListObservers</code>
     *
     * @param events a list of events to be stored in <code>this.eventMap</code>
     */
    public EventManager(List<Event> events) {
        if (events.isEmpty()) {
            this.eventMap = new HashMap<>();
        } else {
            this.eventMap = new HashMap<>();
            for (Event event : events) {
                this.eventMap.put(event.getID(), event);
            }
        }
        this.toUpdate = new EventListObserver[]{};
        this.repeatedEventManager = new RepeatedEventManager();
    }

    /**
     * Get this Events map
     *
     * @return A map of UUID of users as keys and list of events of that user as values
     */
    public Map<UUID, List<Event>> getUuidEventsMap() {
        return this.uuidEventsMap;
    }

    /**
     * Set this Events map to the parameter
     *
     * @param map A map of UUID of users as keys and list of events of that user as values
     */
    public void setUuidEventsMap(Map<UUID, List<Event>> map) {
        this.uuidEventsMap = map;
    }

    /**
     * returns the ID of an Event (does not have to be in <code>this.eventMap</code>
     *
     * @param event any Event
     * @return the ID of the Event (Event.getID())
     */
    public UUID getID(Event event) {
        return event.getID();
    }


    /**
     * getDay returns a map of the events in a day
     *
     * @param day the day that is being searched for
     * @return <code>Map<Integer, Event></code> of all events in this day by ID
     */
    public Map<UUID, Event> getDay(LocalDate day) {
        Map<UUID, Event> dayMap = new HashMap<>();
        for (Event event : eventMap.values()) {
            if (event.getDay().isEqual(day)) {
                dayMap.put(event.getID(), event);
            }
        }
        return dayMap;
    }

    public List<Event> getDay(List<Event> schedule, LocalDate day) {
        List<Event> dayMap = new ArrayList<>();
        for (Event event : schedule) {
            if (event.getDay().isEqual(day)) {
                dayMap.add(event);
            }
        }
        return dayMap;
    }

    /**
     * returns an event in <code>this.eventMap</code> with the input ID if it is there, otherwise returns null
     *
     * @param ID the ID of an event
     * @return the event with this ID, or null
     */
    public Event get(UUID ID) {
        if (this.eventMap.containsKey(ID)) {
            return eventMap.get(ID);
        } else {
            for (Event event: getAllEvents()){
                if (!event.getWorkSessions().isEmpty()){
                    for (Event session: event.getWorkSessions()){
                        if (session.getID().equals(ID)){
                            return session;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * removes the event of this ID from <code>this.eventMap</code> if it is there, returns the removed event or null
     *
     * @param ID the name to be removed
     * @return the event just removed, or null
     */
    public Event remove(UUID ID) {
        this.update("remove", this.get(ID));
        return eventMap.remove(ID);
    }

    public void removeAll(List<UUID> IDs){
        IDs.forEach(eventMap::remove);
    }

    /**
     * creates an event with given name and end time and adds to <code>this.eventMap</code>
     *
     * @param title   String title of the Event
     * @param endTime LocalDateTime end time of the event
     * @return the event that was created with given title, endTime, and unique ID
     */
    public Event addEvent(String title, LocalDateTime endTime) {
        Event event = new Event(UUID.randomUUID(), title, endTime);
        this.addEvent(event);
        return event;
    }


    /**
     * creates an event with given name and end time.
     *
     * @param title   String title of the Event
     * @param endTime LocalDateTime end time of the event
     * @return the event that was created with given title, endTime, and unique ID
     */
    public Event getEvent(String title, LocalDateTime endTime) {
        return new Event(UUID.randomUUID(), title, endTime);
    }

    /**
     * adds an already existing event to <code>this.eventMap</code>. will overwrite event of same ID
     *
     * @param event event to be added
     */
    public void addEvent(Event event) {
        this.eventMap.put(event.getID(), event);
        this.update("add", event);
    }


    /**
     * takes a list of events that may contain work sessions and returns the same list of events in addition to
     * the work sessions they contain
     *
     * @param events List<Event> a list of events that may contain work sessions
     * @return a list of events plus their work sessions
     */
    public List<Event> flattenWorkSessions(List<Event> events) {
        List<Event> flat = new ArrayList<>();
        if (events.isEmpty()) {
            return flat;
        }
        for (Event event : events) {
            flat.add(event);
            if (!event.getWorkSessions().isEmpty()) {
                flat.addAll(event.getWorkSessions());
            }
        }
        return flat;
    }

    /**
     * returns an Event as a list of events, splitting them at 24:00 each day it spans. For an event that spans
     * multiple days, will return an event with startTime = event.startTime, endTime the same day as the startTime
     * but with time = 24:00, and each subsequent event will have start and end 0:00-24:00 for each day the event spans
     * fully. The final event will have start time 0:00 on the date of the endTime, with endTime same as the original
     * events endTime
     *
     * @param event the event to be split, may or may not have start time or span multiple days
     * @return the list of events as split by day
     */
    public List<Event> splitByDay(Event event) {
        if (event.hasStart()) {
            List<Event> splitByDay = new ArrayList<>();
            if (event.getStartTime().toLocalDate().isBefore(event.getEndTime().toLocalDate())) {
                splitByDay.add(new Event(event.getID(), event.getName(), event.getStartTime(),
                        LocalDateTime.of(event.getStartTime().toLocalDate(), LocalTime.of(23, 59))));
                for (LocalDate nextDay = event.getStartTime().plusDays(1L).toLocalDate(); !event.getEndTime().
                        toLocalDate().isBefore(nextDay); nextDay = nextDay.plusDays(1)) {
                    splitByDay.add(new Event(event.getID(), event.getName(), LocalDateTime.of(nextDay, LocalTime.of(0, 0)),
                            LocalDateTime.of(nextDay, LocalTime.of(23, 59))));
                    nextDay = nextDay.plusDays(1L);
                }
                splitByDay.add(new Event(event.getID(), event.getName(), LocalDateTime.of(event.getEndTime().toLocalDate(),
                        LocalTime.of(0, 0)), event.getEndTime()));
                return splitByDay;
            }
        }
        return new ArrayList<>(List.of(new Event[]{event}));
    }


    public RepeatedEventManager getRepeatedEventManager() {
        return repeatedEventManager;
    }

    /**
     * @param recursiveEvent The RecursiveEvent from which the repeated events should be extracted.
     * @return Given a RecursiveEvent, this method returns all the events in the period of repetition specified in the
     * RecursiveEvent object.
     */

    public List<Event> recursiveEventList(RecursiveEvent recursiveEvent){
        List<Event> result = new ArrayList<>();
        for(List<Event> events : repeatedEventManager.getRecursiveIdToDateToEventsMap().get(recursiveEvent.getId()).values()){
            result.addAll(events);
        }
        return result;
    }

    /**
     * returns ArrayList of all events in <code>this.eventMap</code>, including work sessions within events and
     * repeated events, split at day boundaries
     *
     * @return list of events, including work sessions within events (flattened)
     */
    public List<Event> getAllEventsFlatSplit() {
        List<Event> events = new ArrayList<>();
        for (Event event : this.flattenWorkSessions(new ArrayList<>(this.eventMap.values()))) {
            events.addAll(this.splitByDay(event));
        }
        for (RecursiveEvent recursiveEvent : repeatedEventManager.getRecursiveEventMap().values()){
            List<Event> repeatedEvents = recursiveEventList(recursiveEvent);
            for (Event event : repeatedEvents){
                events.addAll(this.splitByDay(event));
            }
        }
        return events;
    }

    public List<Event> flatSplitEvents(List<Event> events) {
        List<Event> splitFlat = new ArrayList<>();
        for (Event event : this.flattenWorkSessions(events)) {
            splitFlat.addAll(this.splitByDay(event));
        }
        return splitFlat;
    }

    /**
     * returns all the values in <code>this.eventMap</code>
     *
     * @return list of events (without work sessions, not split)
     */
    public List<Event> getAllEvents() {
        return this.timeOrder(new ArrayList<>(this.eventMap.values()));
    }

    /**
     * returns the name of any event [event.getName()]
     *
     * @param event any event (does not have to be in <code>this.eventMap</code>
     * @return the name of the event
     */
    public String getName(Event event) {
        return event.getName();
    }

    public void setStart(UUID id, LocalDateTime start) {
        this.get(id).setStartTime(start);
        this.update("change", this.get(id));
    }

    public void setEnd(UUID id, LocalDateTime end) {
        this.get(id).setEndTime(end);
        this.update("change", this.get(id));
    }

    /**
     * updates all eventList observers in <code>this.toUpdate</code> with given parameter. Runs when events are added,
     * removed, or times change in <code>this.eventMap</code>
     *
     * @param addRemoveChange string "add" or "remove" or "change" to specify the nature of the update
     * @param changed         list of the events that are modified
     */
    public void update(String addRemoveChange, Event changed) {
        for (EventListObserver obs : this.toUpdate) {
            obs.update(addRemoveChange, changed, this);
        }
    }

    /**
     * adds an <code>EventListObserver</code> to <code>this.toUpdate</code>
     *
     * @param obs the observer to be added
     */
    public void addObserver(EventListObserver obs) {
        List<EventListObserver> inter = new ArrayList<>(List.of(this.toUpdate));
        inter.add(obs);
        this.toUpdate = inter.toArray(new EventListObserver[0]);
    }

    /**
     * calculates the total hours in a list of events using event.getLength and summing
     *
     * @param events list of events with lengths to be added
     * @return the total length of time in hours spent on these events
     */
    public float totalHours(List<Event> events) {
        float hours = 0;
        for (Event event : events) {
            hours += event.getLength();
        }
        return hours;
    }

    /**
     * computes the earliest startTime in a list of events (chronologically first)
     *
     * @param events the list of events over which to compare startTimes
     * @return the event with the earliest start time
     */
    public Event earliest(List<Event> events) {
        Event earliest = events.get(0);
        LocalDateTime earliestStartTime = earliest.getEndTime();
        if (earliest.hasStart()) {
            earliestStartTime = earliest.getStartTime();
        }
        for (Event event : events) {
            LocalDateTime eventStartTime;
            if (!event.hasStart()) {
                eventStartTime = event.getEndTime();
            } else {
                eventStartTime = event.getStartTime();
            }
            if (eventStartTime.isBefore(earliestStartTime)) {
                earliest = event;
                earliestStartTime = eventStartTime;
            }
        }
        return earliest;
    }

    /**
     * orders a list of events chronologically earliest to latest
     *
     * @param events the list to be modified
     * @return the input list, time ordered
     */
    public List<Event> timeOrder(List<Event> events) {
        List<Event> sorted = new ArrayList<>();
        events = new ArrayList<>(events);
        while (!(events.isEmpty())) {
            sorted.add(earliest(events));
            events.remove(earliest(events));
        }
        events = sorted;
        return events;
    }

    /**
     * Orders a list of event IDs chronologically earliest to latest
     * @param eventIDList the list of event ID to be modified
     * @return the input list, time ordered
     */
    public List<UUID> timeOrderID(List<UUID> eventIDList) {
        List<Event> eventList = new ArrayList<>();
        for (UUID eventID : eventIDList) {
            eventList.add(this.get(eventID));
        }
        eventList = timeOrder(eventList);
        List<UUID> sortedEventID = new ArrayList<>();
        for (Event event : eventList) {
            sortedEventID.add(this.getID(event));
        }
        return sortedEventID;
    }

    public String displayEvent(Event event) {
        return event.toString();
    }

    /**
     * checks if an Event of this ID is contained in <code>this.eventMap</code>
     *
     * @param ID any UUID
     * @return true if an event with this integer ID is in <code>this.eventMap</code>, false otherwise
     */
    public boolean containsID(UUID ID) {

        return !(this.get(ID) == null);
    }

    /**
     * returns all events in <code>this.eventMap</code> whose start times are after input 'from', and whose end times are
     * before input 'to'
     *
     * @param from LocalDate the start day of the range
     * @param to   LocalDate the end day of the range
     * @return Map with key LocalDate for each day between or equal to 'from' and 'to' in range, value all the events
     * in <code>this.eventMap</code> that occur in this day
     */
    public Map<LocalDate, List<Event>> getRange(LocalDate from, LocalDate to) {
        Map<LocalDate, List<Event>> range = new HashMap<>();
        List<Event> schedule = this.getAllEventsFlatSplit();
        long current = 0L;
        while (!from.plusDays(current).isAfter(to)) {
            range.put(from.plusDays(current), (this.getDay(schedule, from.plusDays(current))));
            current += 1L;
        }
        return range;
    }

    /**
     * Sets the name of any event (does not have to be in <code>this.eventMap</code>
     *
     * @param event the event to set name
     * @param name  String of new name
     */
    public void setName(Event event, String name) {
        event.setName(name);
    }

    /**
     * Sets the description of an event, does not have to be in <code>this.eventMap</code>
     *
     * @param event   the event with description to be set
     * @param describe String the new description
     */
    public void setDescription(Event event, String describe) {
        event.setDescription(describe);
    }

    /**
     * Get description of a specific event
     * @param eventID ID of the event
     * @return get description of the event from eventID
     */
    public String getDescription(UUID eventID) {
        if (eventMap.containsKey(eventID)) {
            if (this.eventMap.get(eventID).getDescription() != null) {
                return this.eventMap.get(eventID).getDescription();
            }
            else {
                return "No description provided";
            }
        }
        else {
            return null;
        }
    }

    /**
     * Return the start time information of the chosen event in string
     * @param eventID ID of an event to investigate
     * @return the string of the start time
     */
    public String getStartTimeString(UUID eventID) {
        Event event = this.get(eventID);
        if (event.hasStart()) {
            String[] date = event.getStartTime().toString().split("-");
            return date[2].substring(3, 8);
        } else {
            return null;
        }
    }

    /**
     * Return the end time information of the chosen event in string
     * @param eventID ID of an event to investigate
     * @return the string of the end time
     */
    public String getEndTimeString(UUID eventID) {
        Event event = this.get(eventID);
        String[] date = event.getEndTime().toString().split("-");
        return date[2].substring(3, 8);
    }

    /**
     * Return the session length of the event given by the ID
     * @param id ID of the event
     * @return session length of the event
     */
    public Long getEventSessionLength(UUID id) {
        if (this.containsID(id)) {
            return this.get(id).getSessionLength();
        }
        else {
            return null;
        }
    }

    /**
     * Return the events' total work session list
     * @param id ID of the event
     * @return list of the total work session
     */
    public List<Event> getTotalWorkSession(UUID id) {
        if (this.containsID(id)) {
            return this.timeOrder(this.get(id).getWorkSessions());
        }
        return null;
    }

    /**
     * Return the list of the past work sessions of the event
     * @param id ID of the event
     * @return list of the past work session
     */
    public List<Event> getPastWorkSession(UUID id) {
        if (this.containsID(id)) {
            List<Event> totalWorkSession = this.get(id).getWorkSessions();
            List<Event> pastWorkSession = new ArrayList<>();
            for (Event event : totalWorkSession) {
                if (event.getEndTime().isBefore(LocalDateTime.now())) {
                    pastWorkSession.add(event);
                }
            }
            return this.timeOrder(pastWorkSession);
        }
        else {
            return null;
        }
    }

    /**
     * Return the list of the future work sessions of the event
     * @param id ID of the event
     * @return the list of the future work sessions of the event
     */
    public List<Event> getFutureWorkSession(UUID id) {
        if (this.containsID(id)) {
            List<Event> totalWorkSession = this.get(id).getWorkSessions();
            List<Event> futureWorkSession = new ArrayList<>();
            for (Event event : totalWorkSession) {
                if (event.getEndTime().isAfter(LocalDateTime.now())) {
                    futureWorkSession.add(event);
                }
            }
            return this.timeOrder(futureWorkSession);
        }
        else {
            return null;
        }
    }

    public String getPastSessionsString(UUID id){
        StringBuilder options = new StringBuilder();
        for (Event session: this.getPastWorkSession(id)){
            options.append("\n").append(this.getTotalWorkSession(id).indexOf(session)).append(" ----\n ").append(session);
        }
        return options.toString();
    }

    public String getFutureSessionsString(UUID id){
        StringBuilder options = new StringBuilder();
        for (Event session: this.getFutureWorkSession(id)){
            options.append("\n").append(this.getTotalWorkSession(id).indexOf(session)).append(" ----\n ").append(session);
        }
        return options.toString();
    }

    /**
     * Return the total session hours of the event
     * @param id ID of the event
     * @return the total session hours set by the event
     */
    public Long getTotalHoursNeeded(UUID id) {
        if (this.containsID(id)) {
            return this.get(id).getHoursNeeded();
        }
        else {
            return null;
        }
    }

    /**
     * Return the End date of the event
     * @param id ID of the event
     * @return the end date of the event
     */
    public LocalDate getEndDate(UUID id) {
        return this.get(id).getEndTime().toLocalDate();
    }

    /**
     * Return the start date of the event
     * @param id ID of the event
     * @return the start date of the event
     */
    public LocalDate getStartDate(UUID id) {
        if (this.get(id).hasStart()) {
            return this.get(id).getStartTime().toLocalDate();
        } else {
            return null;
        }
    }


    /**
     * Return the end time of the event
     * @param id ID of the event
     * @return the end time of the event
     */
    public LocalTime getEndTime(UUID id) {
        return this.get(id).getEndTime().toLocalTime();
    }

    /**
     * Return the start time of the event
     * @param id ID of the event
     * @return the start time of the event
     */
    public LocalTime getStartTime(UUID id) {
        if (this.get(id).hasStart()) {
            return this.get(id).getStartTime().toLocalTime();
        } else {
            return null;
        }
    }



    /**
     * Return the end date time of the event
     * @param event selected event
     * @return Return the end date time of the event
     */
    public LocalDateTime getEnd(Event event){
        return event.getEndTime();
    }

    public LocalDateTime getEnd(UUID ID){
        return this.get(ID).getEndTime();
    }

    public LocalDateTime getStart(Event event){
        return event.getStartTime();
    }

    public LocalDateTime getStart(UUID event){
        return this.get(event).getStartTime();
    }


    public void setWorkSessions(UUID ID, List<Event> sessions){
        this.get(ID).setWorkSessions(sessions);
    }

    public List<Event> getPastSessions(UUID ID){
        return this.get(ID).pastWorkSessions();
    }

    public List<Event> getWorkSessions(UUID ID){
        return this.get(ID).getWorkSessions();
    }

    public void removeWorkSession(UUID id, Event session){
        this.getWorkSessions(id).remove(session);
    }

    public void addWorkSession(UUID ID, LocalDateTime start, LocalDateTime end){
        this.get(ID).addWorkSession(start, end);
    }

    public void setSessionLength(UUID ID, Long sessionLength) {
        this.get(ID).setSessionLength(sessionLength);
    }

    public void setHoursNeeded(UUID deadline, Long hoursNeeded) {
        this.get(deadline).setHoursNeeded(hoursNeeded);
    }

    public double getLength(Event event) {
        return event.getLength();
    }

    public double getHoursNeeded(UUID event) {
        return this.get(event).getHoursNeeded();
    }

    public void addAll(List<Event> events) {
        for (Event event: events){
            this.addEvent(event);
        }
    }
    public Map<UUID, Event> getEventMap() { return this.eventMap; }

    public void changeStartWorking(UUID event, LocalDate start){
        this.get(event).setStartWorking(Duration.between(LocalDateTime.of(start, LocalTime.of(0, 0)),
                LocalDateTime.of(this.getEndDate(event), LocalTime.of(0, 0))).toDays());
    }

    public LocalDate getStartWorking(UUID event) {
        return getEndDate(event).minusDays(get(event).getStartWorking());
    }
}