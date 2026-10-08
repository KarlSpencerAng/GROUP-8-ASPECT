package studentmanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Teacher schedules, current enrolled roster, and configurable submitted-grade report. */
public final class TeacherClassInsightsPanel extends JPanel {
    private final int teacherId;
    private final JComboBox<TeacherClassReportRepository.Subject> subjectBox=new JComboBox<>();
    private final DefaultTableModel rosterModel=model("Student #","Student","Percentage","University Grade","Status");
    private final DefaultTableModel scheduleModel=model("ID","Day","Start","End","Room");
    private final JTextField threshold=new JTextField(5);
    private final JLabel stats=new JLabel("Enter your institution's passing percentage and refresh.");
    private final JComboBox<String> day=new JComboBox<>(new String[]{"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"});
    private final JTextField start=new JTextField("08:00",5),end=new JTextField("09:00",5),room=new JTextField(10);
    private List<TeacherClassReportRepository.StudentRow> lastRoster=Collections.emptyList();
    private static DefaultTableModel model(String... names){return new DefaultTableModel(names,0){@Override public boolean isCellEditable(int r,int c){return false;}};}
    public TeacherClassInsightsPanel(int teacherId){
        super(new BorderLayout(8,8));this.teacherId=teacherId;
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Assigned subject:"));top.add(subjectBox);
        JButton refresh=new JButton("Refresh subject, roster and schedule");refresh.addActionListener(e->reload());top.add(refresh);add(top,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();
        JPanel report=new JPanel(new BorderLayout(8,8));
        report.add(new JScrollPane(new JTable(rosterModel)),BorderLayout.CENTER);
        JPanel reportBottom=new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportBottom.add(new JLabel("Passing percentage (required):"));reportBottom.add(threshold);
        JButton calculate=new JButton("Calculate report");calculate.addActionListener(e->calculate());reportBottom.add(calculate);
        reportBottom.add(stats);report.add(reportBottom,BorderLayout.SOUTH);
        tabs.addTab("Class Report",report);
        JPanel schedule=new JPanel(new BorderLayout(8,8));
        schedule.add(new JScrollPane(new JTable(scheduleModel)),BorderLayout.CENTER);
        JPanel controls=new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(day);controls.add(new JLabel("Start HH:mm"));controls.add(start);
        controls.add(new JLabel("End HH:mm"));controls.add(end);controls.add(new JLabel("Room"));controls.add(room);
        JButton add=new JButton("Add schedule");add.addActionListener(e->addSchedule());controls.add(add);
        JButton remove=new JButton("Delete selected schedule");
        JTable scheduleTable=(JTable)((JScrollPane)schedule.getComponent(0)).getViewport().getView();
        remove.addActionListener(e->{int row=scheduleTable.getSelectedRow();if(row<0){warn("Select a schedule first.");return;}
            int id=(Integer)scheduleModel.getValueAt(scheduleTable.convertRowIndexToModel(row),0);
            if(JOptionPane.showConfirmDialog(this,"Delete this schedule?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            try{TeacherScheduleRepository.delete(teacherId,subject().id,id);reloadSchedule();}catch(Exception ex){warn(ex.getMessage());}
        });controls.add(remove);schedule.add(controls,BorderLayout.SOUTH);
        tabs.addTab("Manual Schedule",schedule);add(tabs,BorderLayout.CENTER);
        subjectBox.addActionListener(e->reload());
        try{for(TeacherClassReportRepository.Subject s:TeacherClassReportRepository.subjects(teacherId))subjectBox.addItem(s);}
        catch(Exception ex){warn(ex.getMessage());}
        if(subjectBox.getItemCount()>0)reload();
    }
    private TeacherClassReportRepository.Subject subject(){return (TeacherClassReportRepository.Subject)subjectBox.getSelectedItem();}
    private void warn(String message){JOptionPane.showMessageDialog(this,message,"Class insights",JOptionPane.WARNING_MESSAGE);}
    private void reload(){if(subject()==null)return;
        try{
            lastRoster=TeacherClassReportRepository.roster(teacherId,subject().id);
            rosterModel.setRowCount(0);
            for(TeacherClassReportRepository.StudentRow r:lastRoster)rosterModel.addRow(new Object[]{r.number,r.name,r.percentage,r.numerical,r.status});
            stats.setText("Roster: "+lastRoster.size()+" | Enter passing percentage and calculate.");
            reloadSchedule();
        }catch(Exception ex){warn(ex.getMessage());}
    }
    private void reloadSchedule() throws java.sql.SQLException {
        scheduleModel.setRowCount(0);
        if(subject()==null)return;
        for(TeacherScheduleRepository.Entry e:TeacherScheduleRepository.list(teacherId,subject().id))
            scheduleModel.addRow(new Object[]{e.id,e.day,e.start,e.end,e.room});
    }
    private void calculate(){
        double pass;
        try{pass=Double.parseDouble(threshold.getText().trim());if(!Double.isFinite(pass)||pass<0||pass>100)throw new NumberFormatException();}
        catch(NumberFormatException ex){warn("Enter your institution's passing percentage between 0 and 100.");return;}
        int passed=0,failed=0,draft=0,missing=0;double sum=0;
        for(TeacherClassReportRepository.StudentRow r:lastRoster){
            if(r.percentage==null){missing++;continue;}
            if(!"Submitted".equals(r.status)&&!"Revised".equals(r.status)){draft++;continue;}
            sum+=r.percentage;
            if(r.percentage>=pass)passed++;else failed++;
        }
        int graded=passed+failed;
        stats.setText(String.format(java.util.Locale.US,"Submitted/revised: %d | Pass: %d | Fail: %d | Mean: %s | Draft: %d | Missing: %d",graded,passed,failed,graded==0?"N/A":String.format(java.util.Locale.US,"%.2f%%",sum/graded),draft,missing));
    }
    private void addSchedule(){if(subject()==null){warn("No assigned subject selected.");return;}
        try{TeacherScheduleRepository.add(teacherId,subject().id,(String)day.getSelectedItem(),start.getText(),end.getText(),room.getText());reloadSchedule();}
        catch(Exception ex){warn(ex.getMessage());}
    }
}
