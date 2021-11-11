package presenters;

import helpers.Constants;
import presenters.MenuStrategies.BasicMenuContent;
import presenters.MenuStrategies.CalendarYearMonthMenuContent;
import presenters.MenuStrategies.MenuContent;

public class DisplayMenu {
    private MenuContent menuContent;


    public String displayMenu(MenuContent typeMenuContent) {
        setMenuContent(typeMenuContent);
        StringBuilder result = new StringBuilder();
        String divider = "-".repeat(Constants.MENU_DIVIDER);
        result.append(" ").append(divider).append(" ");
        this.menuContent.addMenuContent(result);
        result.append("\n").append(" ").append(divider);
        return result.toString();
    }

    public void setMenuContent(MenuContent menuContent) {
        this.menuContent = menuContent;
    }



    public static void main(String[] args) {
        DisplayMenu dm = new DisplayMenu();
        MenuContent content = new BasicMenuContent();
        MenuContent calendarContent = new CalendarYearMonthMenuContent();
        System.out.println(dm.displayMenu(content));
        System.out.println(dm.displayMenu(new CalendarYearMonthMenuContent()));
        System.out.println(calendarContent.numberOfOptions());
    }
}