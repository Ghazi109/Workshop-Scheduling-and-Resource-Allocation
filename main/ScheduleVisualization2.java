package main;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import org.chocosolver.solver.variables.IntVar;

public class ScheduleVisualization2 extends JPanel {
    
    private static final Color[] COLORS = {Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.CYAN, Color.MAGENTA};
    
    private List<Integer>[] tasksByStation;
    private IntVar[] durations;
    private IntVar[] startTimes;
    private int Start;
    
    public ScheduleVisualization2(List<Integer>[] tasksByStation, IntVar[] durations, IntVar[] startTimes,int Start) {
        this.tasksByStation = tasksByStation;
        this.durations = durations;
        this.startTimes = startTimes;
        this.Start=Start;
        setPreferredSize(new Dimension(800, 600));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int startX = 50;
        int startY = 50;
        int barHeight = 20;
        
       
        
        int cpt=0;
        for (int i = 0; i < tasksByStation.length; i++) {
        	
        	 int y = startY + i * (barHeight + 10);
             int x = startX-Start;
             int height = barHeight;
             int width = 0;
            
            List<Integer> stationTasks = tasksByStation[i];
            g.drawString("Station "+i , startX - 40, y + barHeight / 2);
            for (int j = 0; j < stationTasks.size(); j++) {
                int taskIndex = stationTasks.get(j);
                if (taskIndex != -1) {
                    x = startX + startTimes[taskIndex].getValue()-Start; // Utilisation de getValue() pour obtenir la valeur actuelle
                    width = durations[taskIndex].getValue();
                    height = barHeight;
                    cpt+=1;
                    
                    Color color = COLORS[cpt % COLORS.length]; // Change color for each worker
                    g.setColor(color);
                    g.fillRect(x, y, width, height);
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, width, height);
                    g.drawString(Integer.toString(startTimes[taskIndex].getValue()), x, y + 20); // Temps de début
                    g.drawString(Integer.toString(startTimes[taskIndex].getValue()+durations[taskIndex].getValue()), x+width, y + 20); // Temps de fin
                    //g.drawString("Worker " + taskIndex, startX - 40, y + barHeight / 2);
                }
            }
        }
    }
    
    public static void displaySchedule(List<Integer>[] tasksByStation, IntVar[] durations, IntVar[] startTimes,int min) {
        JFrame frame = new JFrame("Schedule Visualization");
        ScheduleVisualization2 panel = new ScheduleVisualization2(tasksByStation, durations, startTimes,min);
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
    

}