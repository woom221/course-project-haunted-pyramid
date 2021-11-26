package usecases.events;

import helpers.ConstantID;
import entities.Event;
import entities.recursions.RecursiveEvent;
import interfaces.EventListObserver;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Malik Lahlou
 */

public class RepeatedEventManager implements EventListObserver {
    /**
     *
     * Integer: id of RecursiveEvent object
     * LocalDateTime: the date of the beginning of the list of events List<Event>.
     */


    private  Map<Integer, RecursiveEvent> recursiveEventMap;
    private Map<Integer, Map<LocalDateTime, List<Event>>> RecursiveIdToDateToEventsMap;


    /**
     *
     * Initialize an empty RepeatedEventManager.
     */

    public RepeatedEventManager(){
        this.RecursiveIdToDateToEventsMap = new HashMap<>();
    }

    /**
     *
     * Getter and Setter methods.
     */

    public Map<Integer, Map<LocalDateTime, List<Event>>> getRecursiveIdToDateToEventsMap() {return RecursiveIdToDateToEventsMap;}
    public Map<Integer, RecursiveEvent> getRecursiveEventMap() {return recursiveEventMap;}




    //    /**
//     *
//     * Given a recursive event id, a list of events in one cycle and a method of repetition, add a recursive event
//     * object to the RepeatedEventManager map.
//     */
//
//    public void addRecursiveEvent(RecursiveEvent recursiveEvent){
//        this.recursiveEventMap.put(recursiveEvent.getId(), recursiveEvent);
//    }


    /**
     * @param id The id of the Recursive event.
     */
    public RecursiveEvent getRecursiveEvent(Integer id){
        return this.recursiveEventMap.get(id);
    }

    /**
     *
     * @param id The id of a Recursive event.
     * @return Given the id of a recursive event object, this methods access the events in one cycle of this repetition
     * and returns a map with the id of the original event in the cycle as keys, and the list of events in the period
     * of repetition as values.
     */

    public Map<UUID, List<Event>> getEventMapFromRecursion(Integer id){
        Map<UUID, List<Event>> result = new HashMap<>();
        List<Event> eventsInOneCycle = this.recursiveEventMap.get(id).getEventsInOneCycle();
        int realSize = eventsInOneCycle.size() - 1;
        for(int eventIndex = 0; eventIndex < realSize; eventIndex++){
            Event event = eventsInOneCycle.get(eventIndex);
            result.put(event.getID(), this.getRecursiveEvent(id).createEventInCycles(event));
        }
        return result;
    }


    /**
     *
     * @param id The id of a Recursive event.
     * @return Given the id of a recursive event object, this methods access the events in one cycle of this repetition
     * and returns a map with the id of the original event in the cycle as keys, and the list of events in the period
     * of repetition as values.
     */

    public List<Event> getEventsFromRecursion(Integer id){
        List<Event> eventsInOneCycle = this.recursiveEventMap.get(id).getEventsInOneCycle();
        return this.recursiveEventMap.get(id).listOfEventsInCycles(eventsInOneCycle);
    }



    private LocalDateTime startTimeGetter(Event event){
        if(event.getStartTime() == null){
            return event.getEndTime();
        }
        else{
            return event.getStartTime();
        }
    }

    public void addEventsFromRecursiveEvent(RecursiveEvent recursiveEvent){
        Map<LocalDateTime, List<Event>> datesAndEvents = new HashMap<>();
        int myID = recursiveEvent.getId();
        this.RecursiveIdToDateToEventsMap.put(myID, datesAndEvents);
        int cycleLength = recursiveEvent.getCycleLength();
        List<Event> allEventsInCycles = recursiveEvent.listOfEventsInCycles(recursiveEvent.getEventsInOneCycle());
        int endLoop = allEventsInCycles.size();
        int i = 1;
        while(cycleLength*i < endLoop){
            this.RecursiveIdToDateToEventsMap.get(myID).put(startTimeGetter(allEventsInCycles.get(cycleLength*(i-1))),
                    allEventsInCycles.subList(cycleLength*(i-1), cycleLength*i));
            i++;
        }
        this.RecursiveIdToDateToEventsMap.get(myID).put(startTimeGetter(allEventsInCycles.get(cycleLength*(i-1))),
                allEventsInCycles.subList(cycleLength*(i-1), endLoop));
    }

    private RecursiveEvent recursiveEventConstructor(List<Event> eventsInCycle, int numberOfRepetition){
        RecursiveEvent recursiveEvent = new RecursiveEvent(ConstantID.get());
        recursiveEvent.setEventsInOneCycle(eventsInCycle);
        recursiveEvent.setNumberOfRepetitionDateGetter(numberOfRepetition);
        return recursiveEvent;
    }

    private RecursiveEvent recursiveEventConstructor(List<Event> eventsInCycle, LocalDateTime[] periodOfRepetition){
        RecursiveEvent recursiveEvent = new RecursiveEvent(ConstantID.get());
        recursiveEvent.setIntervalDateDateGetter(periodOfRepetition);
        recursiveEvent.setEventsInOneCycle(eventsInCycle);
        return recursiveEvent;
    }


    public void addEventsFromRecursiveEvent(List<Event> eventsInCycle, int numberOfRepetition){
        RecursiveEvent recursiveEvent = recursiveEventConstructor(eventsInCycle, numberOfRepetition);
        this.addEventsFromRecursiveEvent(recursiveEvent);
    }

    public void addEventsFromRecursiveEvent(List<Event> eventsInCycle, LocalDateTime[] periodOfRepetition){
        RecursiveEvent recursiveEvent = recursiveEventConstructor(eventsInCycle, periodOfRepetition);
        this.addEventsFromRecursiveEvent(recursiveEvent);
    }





    @Override
    public void update(String addRemoveChange, Event changed, EventManager eventManager) {
        if(changed.getRecursiveId() != null){
            int id = changed.getRecursiveId();
            if (addRemoveChange.equals("add")){
                this.RecursiveIdToDateToEventsMap.get(id);
            }
        }
    }
}



