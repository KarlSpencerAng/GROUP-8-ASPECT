/**
 * HistoricalRecordsPanel.java
 * Purpose: Historical academic records interface.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Teacher-only entry screen. Do not add to StudentDashboard. */
public class HistoricalRecordsPanel extends JPanel {
    private final JComboBox<Student> student = new JComboBox<>();
    private final JComboBox<String> course = new JComboBox<>();
    private final JComboBox<String> status = new JComboBox<>(new String[]{"Taken","Incomplete","Exempted/Credited"});
    private final JTextField reference = new JTextField(22);
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Course", "Status", "Reference / reason"},0) {
        @Override public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable table = new JTable(model);

    public HistoricalRecordsPanel() {
        super(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        for(Student s:AcademicData.getStudents()) student.addItem(s);
        for(String option:CurriculumPanel.curriculumOptions()) course.addItem(option);
        JPanel form=new JPanel(new GridLayout(0,2,8,8));
        form.add(new JLabel("Student"));form.add(student);
        form.add(new JLabel("Curriculum course"));form.add(course);
        form.add(new JLabel("Historical status"));form.add(status);
        form.add(new JLabel("Record reference / reason"));form.add(reference);
        JPanel north=new JPanel(new BorderLayout(5,5));
        north.add(new JLabel("Historical Academic Records — database-backed (TEST)"),BorderLayout.NORTH);
        north.add(form,BorderLayout.CENTER);
        add(north,BorderLayout.NORTH);
        add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save=new JButton("Save Record");
        JButton delete=new JButton("Remove Selected Record");
        JButton refresh=new JButton("Refresh");
        JButton loadDemo = new JButton("Load Sample Records");
        JButton resetDemo = new JButton("Reset Sample Records");
        // Demo controls disabled until their database workflow is integrated.

        buttons.add(save);buttons.add(delete);buttons.add(refresh);

        add(buttons,BorderLayout.SOUTH);
        student.addActionListener(e->reload());
        save.addActionListener(e->save());
        delete.addActionListener(e->remove());
        refresh.addActionListener(e->reload());
        reload();
    }
    private String selectedCourseCode(){
        String option=(String)course.getSelectedItem();
        return option==null?null:option.substring(0,option.indexOf(" - "));
    }
    private void save(){
        Student s=(Student)student.getSelectedItem();
        if(s==null||selectedCourseCode()==null)return;
        try {
            HistoricalRecordsRepository.save(s.getStudentNumber(),selectedCourseCode(),
                    (String)status.getSelectedItem(),reference.getText());
            reload();
            JOptionPane.showMessageDialog(this,"Historical record saved. Refresh the student's Curriculum tab.");
        } catch(IllegalArgumentException|IllegalStateException|java.sql.SQLException ex){
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Record not saved",JOptionPane.WARNING_MESSAGE);
        }
    }
    private void remove(){
        int row=table.getSelectedRow();
        Student s=(Student)student.getSelectedItem();
        if(row<0||s==null)return;
        String code=model.getValueAt(table.convertRowIndexToModel(row),0).toString();
        if(JOptionPane.showConfirmDialog(this,"Remove historical record for "+code+"?",
                "Confirm removal",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try {HistoricalRecordsRepository.remove(s.getStudentNumber(),code);reload();}
        catch(IllegalArgumentException|java.sql.SQLException ex){JOptionPane.showMessageDialog(this,ex.getMessage());}
    }
    private void reload(){
        try {AcademicData.refreshHistoricalRecords();}
        catch(java.sql.SQLException ex) {JOptionPane.showMessageDialog(this,ex.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);return;}
        model.setRowCount(0);
        Student s=(Student)student.getSelectedItem();
        if(s==null)return;
        for(AcademicData.HistoricalRecord r:AcademicData.getHistoricalRecords(s.getStudentNumber()))
            model.addRow(new Object[]{r.getCourseCode(),r.getStatus(),r.getNote()});
    }
    private void loadDemo() {
        Student selected = (Student) student.getSelectedItem();
        if (selected == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Load fictional sample records for the selected student?\n"
                + "Existing records and current grades will not be overwritten.\n"
                + "Sample records are NOT verified academic history.",
                "Load sample data", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        int count = AcademicData.loadHistoricalDemo(selected.getStudentNumber());
        reload();
        JOptionPane.showMessageDialog(this, count + " sample record(s) added.\n"
                + "Refresh the student's Curriculum tab to see the changes.");
    }

    private void resetDemo() {
        Student selected = (Student) student.getSelectedItem();
        if (selected == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove only [DEMO SAMPLE] historical records for this student?\n"
                + "Manually entered records, grades and concerns will remain unchanged.",
                "Reset sample data", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        int count = AcademicData.resetHistoricalDemo(selected.getStudentNumber());
        reload();
        JOptionPane.showMessageDialog(this, count + " sample record(s) removed.");
    }

}
