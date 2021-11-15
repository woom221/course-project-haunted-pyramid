package controllers;

import entities.Event;
import entities.recursions.IntervalDateInput;
import entities.recursions.NumberOfRepetitionInput;
import interfaces.DateGetter;
import usecases.events.EventManager;

import java.util.ArrayList;

public class RecursionController {
    private final IOController ioController = new IOController();

    public void createNewRecursion(Event event, EventManager eventManager, EventController eventController){
        boolean done = false;
        while (!done){
            boolean cycleCreation = false;
            ArrayList<Event> cycle = new ArrayList<>();
            cycle.add(event);
            DateGetter methodToGetDates;
            while (!cycleCreation){
                String chooseOrAdd = ioController.getAnswer("enter 'choose' to add an existing event into this recursion" +
                        " and 'new' to create a new event to add to this recursion");
                if (chooseOrAdd.equalsIgnoreCase("choose")){
                    String ids = ioController.getAnswer("enter the number before the name of the event you" +
                            "want to add in this format: num_1-num_2-...-num_n");
                    String[] newIds = ids.split("-");
                    int[] realIds = new int[newIds.length];
                    for (int i = 0; i < realIds.length; i++) {
                        cycle.add(eventManager.get(Integer.parseInt(newIds[i])));
                    }
                }
                else{
                    System.out.println("After creating the event, enter 'save'");
                    eventController.createDefaultEvent();
                    String id = ioController.getAnswer("enter the id of the event just created");
                    Event newEvent = eventManager.get(Integer.parseInt(id));
                    cycle.add(newEvent);
                }
                String doneOrContinue = ioController.getAnswer("enter 'done' or 'continue' adding events");
                if (doneOrContinue.equalsIgnoreCase("done")){
                    cycleCreation = true;
                    String secondFirstEventDate = ioController.getAnswer("enter the date of the second occurrence " +
                            "of the first event in the form YYYY-MM-DDTHH:MM (must be after all the events added " +
                            "previously)");
                    Event newEvent = eventManager.addEvent(event.getName(), secondFirstEventDate);
                    cycle.add(newEvent);
                }
            }
            eventManager.timeOrder(cycle);
            String repetitionMethod = ioController.getAnswer("Enter either: 'num' if their is the number of " +
                    "times this cycle repeats, or 'dates' if there are two dates in between which the cycle repeats");
            if (repetitionMethod.equalsIgnoreCase("num")){
                String numOfRepetitions = ioController.getAnswer("Enter the number of times the cycle repeats");
                methodToGetDates = new NumberOfRepetitionInput(Integer.parseInt(numOfRepetitions));
            }
            else{
                String beginningOfCycles = ioController.getAnswer("Enter the date when this cycle should begin" +
                        "in the form YYYY-MM-DDTHH:MM");
                String endOfCycles = ioController.getAnswer("Enter the date when this cycle should end" +
                        "in the form YYYY-MM-DDTHH:MM");
                methodToGetDates = new IntervalDateInput(eventManager.stringToDate(beginningOfCycles),
                        eventManager.stringToDate(endOfCycles));
            }
            eventManager.getRepeatedEventManager().addRecursiveEvent(cycle, methodToGetDates);
            done = true;
        }
    }






//    System.out.println();
//            System.out.println("e.g. weekly: mon-4:30-6:30, thu-2:30-8:30, fri-2:30-8:30");
//            System.out.println("monthly: 4-17:50-19:50, 8-12:30-17:30");
//            System.out.println("daily: 4:30-5:30, 19:30-21:30");
//            System.out.println("yearly: OCT-1-1:00-2:00, AUG-10-12:00-14:00");
//    String toEdit = IOController.getAnswer("enter the field you would like to edit followed by its new " +
//            "value, or enter 'done'");
//    String[] toEditList = toEdit.split(": ");
//            if (toEditList[0].equalsIgnoreCase("recursion")){
//        String forall = IOController.getAnswer("would you like this to apply to all or future events " +
//                "\n (please enter 'all' or 'future'?");
//        if (forall.equalsIgnoreCase("all")){
//            //TODO method to delete all instances of event except original and replace with new recursions
//        }else if (forall.equalsIgnoreCase("future")){
//            //TODO method to make this event the original, delete all future instances, and replace with new recursion
//        }
//    } else if(toEditList[0].equalsIgnoreCase("end")){
//        //TODO method to change the end of the recursion and schedule more / remove events accordingly
//    } else if (toEditList[0].equalsIgnoreCase("done")){
//        done = true;
//    }
}
