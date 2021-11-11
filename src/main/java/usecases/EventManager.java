package usecases;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.time.LocalDate;

import entities.ConstantID;
import entities.Event;
import entities.RecursiveEvent;
import interfaces.EventListObserver;

/**
 * @author Taite Cullen
 * @author Malik Lahlou
 * stores and Manages events
 * edits Events by dictionary key (unique ID)
 * updates EventListObservers when Event is added to its eventMap, removed from its eventMap, or
 * the start or end time of an <code>Event</code> within its <code>eventMap</code> is modified
 */
public class EventManager{
    private final Map<Integer, Event> eventMap;
    private RepeatedEventManager repeatedEventManager;
    private EventListObserver[] toUpdate;

    /**
     * constructs event manager. stores list of events by key: ID, value: event in <code>this.eventMap</code>
     * sets <code>this.toUpdate</code> to empty list of <code>EventListObservers</code>
     * @param events a list of events to be stored in <code>this.eventMap</code>
     */
    public EventManager(ArrayList<Event> events){
        if (events.isEmpty()) {
            this.eventMap = new HashMap<>();
        } else {
            this.eventMap = new HashMap<>();
            for (Event event: events){
                this.eventMap.put(event.getID(), event);
            }
        }
        this.toUpdate = new EventListObserver[]{};
    }


    /**
     * constructs empty EventManager (no Events or Observers)
     */
    public EventManager() {
        this.eventMap = new HashMap<>();
        this.toUpdate = new EventListObserver[]{};
    }

    /**
     * returns the ID of an Event (does not have to be in <code>this.eventMap</code>
     * @param event any Event
     * @return the ID of the Event (Event.getID())
     */
    public Integer getID(Event event) {
        return event.getID();

    }

    /**
     * getDay returns a map of the events in a day
     * @param day the day that is being searched for
     * @return <code>Map<Integer, Event></code> of all events in this day by ID
     */
    public Map<Integer, Event> getDay(LocalDate day){
        Map<Integer, Event> dayMap = new HashMap<>();
        for (Event event: eventMap.values()){
            if (event.getDay().isEqual(day)) {
                dayMap.put(event.getID(), event);
            }
        }
        return dayMap;
    }

    /**
     * returns an event in <code>this.eventMap</code> with the input ID if it is there, otherwise returns null
     * @param ID the ID of an event
     * @return the event with this ID, or null
     */
    public Event get(Integer ID){
        if (this.containsID(ID)){
            return eventMap.get(ID);
        }else {
            return null;
        }
    }

    /**
     * removes the event of this ID from <code>this.eventMap</code> if it is there, returns the removed event or null
     * @param ID the name to be removed
     * @return the event just removed, or null
     */
    public Event remove(Integer ID){
        ArrayList<Event> truc = new ArrayList<>();
        truc.add(this.get(ID));
        this.update("remove", truc);
        return eventMap.remove(ID);
    }


    /**
     * creates an event of the given parameters and adds it to <code>this.eventMap</code> by unique ID
     * @param name String name of event
     * @param start LocalDateTime start time of event
     * @param end LocalDateTime end time of event
     */
    public Event addEvent(String name, LocalDateTime start, LocalDateTime end){
        Event event = new Event(ConstantID.get(), name, start, end);
        this.eventMap.put(event.getID(), event);
        ArrayList<Event> truc = new ArrayList<>();
        truc.add(event);
        this.update("add", truc);
        return event;

    }

    /**
     *
     * @param title
     * @param endTime
     * @return
     */
    public Event addEvent(String title, LocalDateTime endTime) {
        Event event = new Event(ConstantID.get(), title, endTime);
        this.addEvent(event);
        return event;
    }

    public Event addEvent(String title, String endString){
        Event event = new Event(ConstantID.get(), title, this.stringToDate(endString));
        this.addEvent(event);
        return event;
    }


    /**
     * takes a list of events that may contain work sessions and returns the same list of events in addition to
     * the work sessions they contain
     * @param events List<Event> a list of events that may contain work sessions
     * @return a list of events plus their work sessions
     */
    private List<Event> flattenWorkSessions(List<Event> events){
        List<Event> flat = new ArrayList<>();
        for (Event event: events){
            flat.add(event);
            if (!event.getWorkSessions().isEmpty()){flat.addAll(event.getWorkSessions());}
        }
        return flat;
    }

    private List<Event> splitByDay(Event event){
        if (event.hasStart()){
            List<Event> splitByDay = new ArrayList<>();
            if (event.getStartTime().toLocalDate().isBefore(event.getEndTime().toLocalDate())){
                splitByDay.add(new Event(event.getID(), event.getName(), event.getStartTime(),
                        LocalDateTime.of(event.getStartTime().toLocalDate(), LocalTime.of(24, 0))));
                LocalDate nextDay = event.getStartTime().plusDays(1L).toLocalDate();
                while(event.getEndTime().toLocalDate().isBefore(nextDay)){
                    splitByDay.add(new Event(event.getID(), event.getName(), LocalDateTime.of(nextDay, LocalTime.of(0, 0)),
                            LocalDateTime.of(nextDay, LocalTime.of(24, 0))));
                    nextDay = nextDay.plusDays(1L);
                }
                splitByDay.add(new Event (event.getID(), event.getName(),
                        LocalDateTime.of(nextDay, LocalTime.of(0, 0)), event.getEndTime()));
            }
            return splitByDay;
        }
        return new ArrayList<>(List.of(new Event[] {new Event (event.getID(), event.getName(), event.getEndTime())}));
    }

    /**
     * returns ArrayList of all events in <code>this.eventMap</code>, including work sessions within events
     * @return  list of events, including work sessions within events (flattened)
     */
    public List<Event> getAllEventsFlatSplit() {
        List<Event> events = new ArrayList<>();
        for (Event event: this.flattenWorkSessions(new ArrayList<>(this.eventMap.values()))){
            events.addAll(this.splitByDay(event));
        }
        return events;
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(this.eventMap.values());
    }

    /**
     * returns the name of any event [event.getName()]
     * @param event any event (does not have to be in <code>this.eventMap</code>
     * @return the name of the event
     */
    public String getName(Event event){
        return event.getName();
    }

    /**
     * sets the start time of the event by splitting an input String of the format:
     * <code>
     *     YYYY-MM-DD HH:MM
     * </code>
     * @param event any event (does not have to be in <code>this.eventMap</code>
     * @param startString String in the form YYYY-MM-DD HH:MM
     */
    public void setStart(Event event, String startString){
        event.setStartTime(stringToDate(startString));
        ArrayList<Event> truc = new ArrayList<>();
        truc.add(event);
        this.update("change", truc);
    }

    /**
     * sets the end time of the event by splitting an input String of the format:
     * <code>
     *     YYYY-MM-DD HH:MM
     * </code>
     * @param event any event (does not have to be in <code>this.eventMap</code>
     * @param endString in the form YYYY-MM-DD HH:MM
     */
    public void setEnd(Event event, String endString){
        event.setEndTime(stringToDate(endString));
        ArrayList<Event> truc = new ArrayList<>();
        truc.add(event);
        this.update("change", truc);
    }
    /**
     * private helper method to convert a String to a LocalDateTime by splitting an input String of the format:
     * <code>
     *     YYYY-MM-DD HH:MM
     * </code>
     * @param dateString String in the form YYYY-MM-DD HH:MM
     * @return LocalDateTime with year YYYY, month MM, day DD, hour HH, minute MM
     */
    private LocalDateTime stringToDate(String dateString) {
        String[] full = dateString.split(" ");
        String[] time = full[1].split(":");
        String[] date = full[0].split("-");
        return LocalDateTime.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]),
                Integer.parseInt(date[2]), Integer.parseInt(time[0]), Integer.parseInt(time[1]));
    }

    public void update(String addRemoveChange, ArrayList<Event> changed){
        for (EventListObserver obs: this.toUpdate){
            obs.update(addRemoveChange, changed, this);
        }
    }

    public void addObserver(EventListObserver obs){
        ArrayList<EventListObserver> inter = new ArrayList<>(List.of(this.toUpdate));
        inter.add(obs);
        this.toUpdate = inter.toArray(new EventListObserver[0]);
    }

    public float totalHours(List<Event> events){
        float hours = 0;
        for(Event event: events) {hours += event.getLength();}
        return hours;
    }

    public Event earliest(List<Event> events){
        Event earliest = events.get(0);
        LocalDateTime earliestStartTime = earliest.getStartTime();
        if (!earliest.hasStart()){
            earliestStartTime = earliest.getEndTime();
        }
        for (Event event: events){
            LocalDateTime eventStartTime = event.getStartTime();
            if (!event.hasStart()){
                eventStartTime = event.getEndTime();
            }
            if (eventStartTime.isBefore(earliestStartTime)){
                earliest = event;
            }
        }
        return earliest;
    }
    public List<Event> timeOrder(List<Event> events){
        List<Event> sorted = new ArrayList<>();
        events = new ArrayList<>(events);
        while (!(events.isEmpty())){
            sorted.add(earliest(events));
            events.remove(earliest(events));
        }
        events = sorted;
        return events;
    }

    public String getDescription(Event event){
        return event.getDescription();
    }

    public String displayEvent(Integer ID){
        return this.get(ID).toString();
    }

    public boolean containsID(Integer ID){
        return this.eventMap.containsKey(ID);
    }

    public Map<LocalDate, List<Event>> getRange(LocalDate from, LocalDate to){
        Map<LocalDate, List<Event>> range = new HashMap<>();
        while (from.isBefore(to)){
            range.put(from, this.flattenWorkSessions(List.of(this.getDay(from).values().toArray(new Event[0]))));
            from  = from.plusDays(1L);
        }
        return range;
    }

    public Map<LocalDateTime, Long> freeSlots(LocalDateTime start, List<Event> events, LocalDateTime end){
        List<Event> schedule = this.timeOrder(this.flattenWorkSessions(events));
        Map<LocalDateTime, Long> lengthStart = new HashMap<>();
        int taskNum = 0;
        for (Event event: schedule){
            if (event.getEndTime().isBefore(end) && event.getStartTime().isAfter(start)){
                if (lengthStart.isEmpty()){
                    lengthStart.put(start, Duration.between(start, event.getStartTime()).getSeconds());
                }else{
                    lengthStart.put(schedule.get(taskNum-1).getStartTime(),
                            Duration.between(schedule.get(taskNum-1).getEndTime(), event.getStartTime()).getSeconds());
                }
            }if(schedule.get(taskNum + 1).getStartTime().isAfter(end)){
                lengthStart.put(event.getEndTime(), Duration.between(event.getEndTime(), end).getSeconds());
            }
            taskNum ++;
        }
        return lengthStart;
    }

    public void setName(Event event, String name) {
        event.setName(name);
    }

    public void setDescription(Event event, String descrip) {
        event.setDescription(descrip);
    }


    public void addEvent(Event event){this.eventMap.put(event.getID(), event);}

    public void setRepeatedEventManager(RepeatedEventManager repeatedEventManager) {
        this.repeatedEventManager = repeatedEventManager;
    }

    /**
     * TODO Malik
     * Given a RecursiveEvent, this method gets all the events in the period of repetition and adds them to the
     * event manager event list.
     */

    public void addEventsInRecursion(RecursiveEvent recursiveEvent){
        for(ArrayList<Event> events : repeatedEventManager.getEventsFromRecursion(recursiveEvent.getId()).values()){
            for(Event event : events){
                this.addEvent(event);
            }
        }
    }

    /**
     * TODO Malik
     */
    public void addEventsInRecursion() {
        for (RecursiveEvent recursiveEvent : this.repeatedEventManager.getRecursiveEventMap().values()) {
            this.addEventsInRecursion(recursiveEvent);
        }
    }

    /**
     * return the start time information of the chosen event in string
     * @param event event to investigate
     * @return the string of the start time
     */
    public String getStartTimeString(Event event){
        if (event.hasStart()){
            String[] date = event.getStartTime().toString().split("-");
            return date[2].substring(3, 8);
        } else {
            return null;
        }
    }

    /**
     * return the end time information of the chosen event in string
     * @param event event to investigate
     * @return the string of the end time
     */
    public String getEndTimeString(Event event){
        String[] date = event.getEndTime().toString().split("-");
        return date[2].substring(3, 8);
    }

}
