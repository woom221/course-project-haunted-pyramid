import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.*;

public class Event {
    // Event contains a name, startTime, endTime
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate date;
    private TemporalUnit MINUTES;

    /**
     * construct a new Event object
     * takes a name argument, a startTime and an endTime
     *
     * @param name the name of the Event
     * @param year the start date and time of the event
     * @param month the end date and time of the event
     */
    public Event(String name, int year, int month, int day, int start_hour, int start_minute, int end_hour,
                 int end_minute){
        this.name = name;
        this.startTime= LocalTime.of(start_hour, start_minute, 0);
        this.endTime = LocalTime.of(end_hour, end_minute, 0);
        this.date = LocalDate.of(year, month, day);
    }

    /**
     * getter for event name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * getter for full start time
     * @return time
     */
    public LocalTime getStartTime() {
        return this.startTime;
    }
    /**
     * getter for end time
     * @return time
     */
    public LocalTime getEndTime() {
        return this.endTime;
    }
    /**
     * getter for event length (hours)
     * @return length (hours)
     */
    public Duration getLength (){
        return Duration.between(this.startTime, this.endTime);
    }

    /**
     * change the start  and end time
     * @param newStart the new start time
     * @param newEnd the new end time
     */
    public void reschedule(LocalTime newStart, LocalTime newEnd){

    }
    /**
     * change the start time, preserve length
     * @param newStart the new start time
     */
    public void reschedule(LocalTime newStart){
        this.endTime = newStart.plus(this.getLength());
        this.startTime = newStart;
    }

    /**
     * set name
     * @param name change event nam
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * set the date
     * @param year year of date
     * @param month month of date
     * @param day day of month
     */
    public void setDate(int year, int month, int day) {
        this.date = LocalDate.of(year, month, day);
    }

    /**
     *
     * @return date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * check if this event conflicts with another event
     * @param other the compared event
     * @return true if this event time/date conflicts with another event
     */
    public boolean conflicts(Event other){
        if (other.getDate().equals(this.getDate())){
            return other.startTime.isBefore(this.startTime) && this.startTime.isBefore(other.endTime);
        }
        return false;
    }

}
