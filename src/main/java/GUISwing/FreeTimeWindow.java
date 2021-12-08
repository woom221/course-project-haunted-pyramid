package GUISwing;

import controllers.UserController;
import interfaces.MeltParentWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FreeTimeWindow implements ActionListener {
    UserController userController;
    final JFrame frame;
    private final MenuCreationHelper helper;

    private Map<JComboBox<LocalTime>, JComboBox<LocalTime>> freeTimes;

    JButton add = new JButton("add");
    JButton save = new JButton("save");

    JPanel freeTimePanel = new JPanel();
    JScrollPane eventScroller = new JScrollPane(freeTimePanel);

    MeltParentWindow parent;

    public FreeTimeWindow(UserController userController, MeltParentWindow parent) {
        this.parent = parent;
        this.userController = userController;
        frame = new PopUpWindowFrame();
        this.helper = new MenuCreationHelper();
        showFreeTime();
        frame.add(eventScroller);
        saveButton();

        frame.setVisible(true);
    }

    private void showFreeTime(){
        freeTimes = new HashMap<>();
        freeTimePanel.setLayout(new BoxLayout(freeTimePanel, BoxLayout.Y_AXIS));

        for (LocalTime start: userController.getCurrentFreeTime().keySet()){
            JPanel freeTime = addFreeTime(start, userController.getCurrentFreeTime().get(start));
            freeTimePanel.add(freeTime);
        }
        eventScroller.setPreferredSize(new Dimension(150, 100));
        eventScroller.setBounds(0, 0, frame.getWidth(), frame.getHeight()-100);
        eventScroller.setVisible(true);
    }

    private JPanel addFreeTime(LocalTime start, LocalTime end){
        JPanel freeTime = new JPanel(new FlowLayout());
        freeTime.setPreferredSize(new Dimension(200, 30));
        JComboBox<LocalTime> startTime = helper.timeComboBox();
        JComboBox<LocalTime> endTime = helper.timeComboBox();
        startTime.setSelectedItem(start);
        endTime.setSelectedItem(end);
        JButton delete = new JButton("delete");
        freeTime.add(new JLabel("start: "));
        freeTime.add(startTime);
        freeTime.add(new JLabel("end: "));
        freeTime.add(endTime);
        freeTime.add(delete);
        delete.setActionCommand("delete-" + Objects.requireNonNull(startTime.getSelectedItem()));
        delete.addActionListener(this);
        freeTimes.put(startTime, endTime);
        return freeTime;
    }



    private void saveButton() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBounds(0, frame.getHeight()-100, frame.getWidth(), 100);
        save = new JButton("save");
        save.addActionListener(this);
        add = new JButton("add");
        add.addActionListener(this);
        buttonPanel.add(add);
        buttonPanel.add(save);

        frame.add(buttonPanel);
    }

    private void refresh(){
      freeTimePanel.removeAll();
      showFreeTime();
      frame.revalidate();
      frame.repaint();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == add){
            freeTimePanel.add(addFreeTime(LocalTime.of(0, 0), LocalTime.of(0, 0)));
            frame.revalidate();
            frame.repaint();
        } else if(e.getSource() == save){
            userController.getCurrentFreeTime().clear();
            for(JComboBox<LocalTime> start: freeTimes.keySet()){
                userController.addFreeTime(( LocalTime) Objects.requireNonNull(start.getSelectedItem()),
                        (LocalTime) freeTimes.get(start).getSelectedItem());
            }
            parent.refresh();
            frame.dispose();
        } else{
            String key = e.getActionCommand().split("-")[1];
            String[] hourMin = key.split(":");
            userController.getCurrentFreeTime().remove(LocalTime.of(Integer.parseInt(hourMin[0]), Integer.parseInt(hourMin[1])));
            refresh();
        }
    }
}
