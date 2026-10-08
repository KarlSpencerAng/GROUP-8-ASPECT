package studentmanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/** Displays existing database grades without altering submission or grading rules. */
public final class AdvancedStudentGradesPanel extends JPanel {
    private final String studentNumber;
    private final JComboBox<String> period = new JComboBox<>();
    private final JComboBox<String> visibility = new JComboBox<>(new String[]{"Posted grades only"});
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
        "Curriculum period", "Course", "Course name", "Section (student)", "Units", "Percentage", "University grade", "Completion", "Status"},0) {
        @Override public boolean isCellEditable(int row,int col){return false;}
    };
    private final JLabel summary = new JLabel(" ");
    private final JLabel cumulative = new JLabel(" ");
    private List<StudentAcademicGradesRepository.Row> rows=Collections.emptyList();
    public AdvancedStudentGradesPanel(String studentNumber) {
        super(new BorderLayout(8,8)); this.studentNumber=studentNumber;
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JPanel top=new JPanel(new BorderLayout());
        JPanel filters=new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("Curriculum period:")); filters.add(period);
        filters.add(new JLabel("Display:")); filters.add(visibility);
        JButton refresh=new JButton("Refresh from database"); filters.add(refresh);
        top.add(filters,BorderLayout.NORTH);
        JLabel note=new JLabel("Curriculum year/term are NOT academic school-year records. Section is the student's current section.");
        note.setForeground(new Color(120,75,0)); top.add(note,BorderLayout.SOUTH);
        add(top,BorderLayout.NORTH);
        JTable table=new JTable(model); table.setRowHeight(25); table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths={155,110,245,130,60,100,110,100,105};
        for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new GridLayout(3,1,0,6));
        bottom.add(summary); bottom.add(cumulative);
        bottom.add(new JLabel("Only Registrar-posted grades are included in official academic results."));
        add(bottom,BorderLayout.SOUTH);
        period.addActionListener(e->render()); visibility.addActionListener(e->render());
        refresh.addActionListener(e->reload()); reload();
    }
    private void reload() {
        try {
            rows=StudentAcademicGradesRepository.load(studentNumber);
            String old=(String)period.getSelectedItem();
            period.removeAllItems(); period.addItem("All curriculum periods");
            LinkedHashSet<String> periods=new LinkedHashSet<>();
            for(StudentAcademicGradesRepository.Row r:rows)periods.add(r.period());
            for(String p:periods)period.addItem(p);
            if(old!=null)period.setSelectedItem(old);
            if(period.getSelectedIndex()<0)period.setSelectedIndex(0);
            render();
        } catch(SQLException ex){
            JOptionPane.showMessageDialog(this,"Unable to load grades: "+ex.getMessage(),"Database error",JOptionPane.ERROR_MESSAGE);
        }
    }
    private boolean visible(StudentAcademicGradesRepository.Row r) {
        return r.official();
    }
    private static String fmt(double d){return String.format(Locale.US,"%.2f",d);}
    private static String averages(List<StudentAcademicGradesRepository.Row> values) {
        double sum=0, weighted=0, units=0; int count=0;
        for(StudentAcademicGradesRepository.Row r:values){
            if(r.units<0)continue;
            sum+=r.universityGrade; count++;
            if(r.units>0){weighted+=r.universityGrade*r.units; units+=r.units;}
        }
        return "Simple: "+(count==0?"N/A":fmt(sum/count))+"   |   Unit-weighted GWA: "+(units==0?"N/A":fmt(weighted/units))+
            "   |   Courses: "+count+"   |   Weighted units: "+(int)units;
    }
    private void render() {
        if(model==null)return;
        model.setRowCount(0);
        String chosen=(String)period.getSelectedItem();
        List<StudentAcademicGradesRepository.Row> selected=new ArrayList<>(), all=new ArrayList<>();
        for(StudentAcademicGradesRepository.Row r:rows){
            if(!visible(r))continue;
            all.add(r);
            if(chosen!=null&&!chosen.equals("All curriculum periods")&&!r.period().equals(chosen))continue;
            selected.add(r);
            model.addRow(new Object[]{r.period(),r.code,r.name,r.section==null?"Not recorded":r.section,r.units,
                fmt(r.percent)+"%",fmt(r.universityGrade),r.complete?"Calculated":"Incomplete","Posted"});
        }
        String scope=chosen==null?"All curriculum periods":chosen;
        summary.setText("Selected ("+scope+"): "+averages(selected));
        cumulative.setText("Cumulative (all available curriculum periods): "+averages(all));
    }
}
