package main;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import org.chocosolver.solver.variables.IntVar;

public class ScheduleVisualization extends JPanel {
    
    private static final Color[] COLORS = {Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.CYAN, Color.MAGENTA};
    
    private List<Integer>[] tasksByWorker;
    private IntVar[] durations;
    private IntVar[] startTimes;
    private int Start;
    private List<int[]>[] WorkerPause;
    
    public ScheduleVisualization(List<Integer>[] tasksByWorker, IntVar[] durations, IntVar[] startTimes,int Start,List<int[]>[] WorkerPauses) {
        this.tasksByWorker = tasksByWorker;
        this.durations = durations;
        this.startTimes = startTimes;
        this.Start=Start;
        this.WorkerPause=WorkerPauses;
        setPreferredSize(new Dimension(800, 600));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int startX = 50;
        int startY = 50;
        int barHeight = 20;
        
        int cpt=0;
        for (int i = 0; i < tasksByWorker.length; i++) {
            int y = startY + i * (barHeight + 10);
            List<Integer> workerTasks = tasksByWorker[i];
            g.drawString("Worker "+i , startX - 40, y + barHeight / 2);
            for (int j = 0; j < workerTasks.size(); j++) {
                int taskIndex = workerTasks.get(j);
                if (taskIndex != -1) {
                    int x = startX + startTimes[taskIndex].getValue()-Start; // Utilisation de getValue() pour obtenir la valeur actuelle
                    int width = durations[taskIndex].getValue();
                    int height = barHeight;
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
            int [] Pause =WorkerPause[i].get(0);
            int x=startX + Pause[0]-Start;
            int width=Pause[1]-Pause[0];
            int height = barHeight;
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, height);
        }
    }
    
    public static void displaySchedule(List<Integer>[] tasksByWorker, IntVar[] durations, IntVar[] startTimes,int min,List<int[]>[] WorkerPause) {
        JFrame frame = new JFrame("Schedule Visualization");
        ScheduleVisualization panel = new ScheduleVisualization(tasksByWorker, durations, startTimes,min,WorkerPause);
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    

}

