package studentmanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Separate controlled workflow: ordinary OLA editor remains locked for submitted grades. */
public final class PostSubmissionAssessmentPanel extends JPanel {
    private final int teacherId;
    private final JComboBox<PostSubmissionAssessmentRepository.Subject> subjects=new JComboBox<>();
    private final DefaultTableModel weights=new DefaultTableModel(new String[]{"ID","Existing assessment","Current weight %","New weight %"},0){
        public boolean isCellEditable(int row,int col){return col==3;}
    };
    private final JTable weightTable=new JTable(weights);
    private final DefaultTableModel students=new DefaultTableModel(new String[]{"Recalculate?","Student ID","Student","New assessment score"},0){
        public Class<?> getColumnClass(int col){return col==0?Boolean.class:String.class;}
        public boolean isCellEditable(int row,int col){return col==0||col==3;}
    };
    private final JTable studentTable=new JTable(students);
    private final JTextField name=new JTextField(16),maximum=new JTextField("100",6),newWeight=new JTextField("10",5);
    private final JTextArea reason=new JTextArea(2,40);
    private List<PostSubmissionAssessmentRepository.Activity> currentActivities=Collections.emptyList();
    private List<PostSubmissionAssessmentRepository.StudentRow> currentStudents=Collections.emptyList();
    public PostSubmissionAssessmentPanel(int teacher){
        this.teacherId=teacher;setLayout(new BorderLayout(8,8));setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));top.add(new JLabel("Assigned subject:"));top.add(subjects);
        JButton refresh=new JButton("Refresh");top.add(refresh);add(top,BorderLayout.NORTH);
        JPanel middle=new JPanel(new GridLayout(2,1,8,8));
        JPanel assessment=new JPanel(new BorderLayout(4,4));assessment.setBorder(BorderFactory.createTitledBorder("New assessment and reweight existing assessments (total must be 100%)"));
        JPanel fields=new JPanel(new FlowLayout(FlowLayout.LEFT));fields.add(new JLabel("Name:"));fields.add(name);fields.add(new JLabel("Maximum:"));fields.add(maximum);fields.add(new JLabel("New weight %:"));fields.add(newWeight);assessment.add(fields,BorderLayout.NORTH);assessment.add(new JScrollPane(weightTable),BorderLayout.CENTER);middle.add(assessment);
        JPanel roster=new JPanel(new BorderLayout(4,4));roster.setBorder(BorderFactory.createTitledBorder("Select students and enter their new scores; unselected submitted grades remain unchanged"));roster.add(new JScrollPane(studentTable),BorderLayout.CENTER);middle.add(roster);add(middle,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(4,4));bottom.add(new JLabel("Required reason for assessment and grade revisions:"),BorderLayout.NORTH);bottom.add(new JScrollPane(reason),BorderLayout.CENTER);
        JButton save=new JButton("Confirm: Add Assessment and Recalculate SELECTED Students");bottom.add(save,BorderLayout.SOUTH);add(bottom,BorderLayout.SOUTH);
        refresh.addActionListener(e->loadSubjects());subjects.addActionListener(e->loadSelected());save.addActionListener(e->submit());
        loadSubjects();
    }
    private void loadSubjects(){
        Object previous=subjects.getSelectedItem();Integer id=previous instanceof PostSubmissionAssessmentRepository.Subject?((PostSubmissionAssessmentRepository.Subject)previous).id:null;
        try{
            subjects.removeAllItems();for(PostSubmissionAssessmentRepository.Subject s:PostSubmissionAssessmentRepository.subjects(teacherId))subjects.addItem(s);
            if(id!=null)for(int i=0;i<subjects.getItemCount();i++)if(subjects.getItemAt(i).id==id){subjects.setSelectedIndex(i);break;}
            loadSelected();
        }catch(Exception e){error(e);}
    }
    private void loadSelected(){
        PostSubmissionAssessmentRepository.Subject s=(PostSubmissionAssessmentRepository.Subject)subjects.getSelectedItem();weights.setRowCount(0);students.setRowCount(0);
        if(s==null)return;
        try{
            currentActivities=PostSubmissionAssessmentRepository.activities(teacherId,s.id);
            currentStudents=PostSubmissionAssessmentRepository.students(teacherId,s.id);
            for(PostSubmissionAssessmentRepository.Activity a:currentActivities)weights.addRow(new Object[]{String.valueOf(a.id),a.name,a.weight.toPlainString(),a.weight.toPlainString()});
            for(PostSubmissionAssessmentRepository.StudentRow student:currentStudents)students.addRow(new Object[]{false,student.number,student.name+" ("+student.status+")",""});
        }catch(Exception e){error(e);}
    }
    private void submit(){
        PostSubmissionAssessmentRepository.Subject subject=(PostSubmissionAssessmentRepository.Subject)subjects.getSelectedItem();if(subject==null){JOptionPane.showMessageDialog(this,"Choose an assigned subject.");return;}
        try{
            if(weightTable.isEditing())weightTable.getCellEditor().stopCellEditing();
            if(studentTable.isEditing())studentTable.getCellEditor().stopCellEditing();
            Map<Integer,String> existing=new LinkedHashMap<>();for(int i=0;i<currentActivities.size();i++)existing.put(currentActivities.get(i).id,String.valueOf(weights.getValueAt(i,3)));
            Map<Integer,String> selected=new LinkedHashMap<>();for(int i=0;i<currentStudents.size();i++)if(Boolean.TRUE.equals(students.getValueAt(i,0)))selected.put(currentStudents.get(i).id,String.valueOf(students.getValueAt(i,3)));
            if(selected.isEmpty()){JOptionPane.showMessageDialog(this,"Select at least one student.");return;}
            int choice=JOptionPane.showConfirmDialog(this,"Create this assessment and recalculate ONLY "+selected.size()+" selected submitted grade(s)?\nExisting assessment weights will change for the whole subject.\nUnselected official grades will NOT be recalculated.\nThis operation is audited.","Confirm selective recalculation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(choice!=JOptionPane.YES_OPTION)return;
            PostSubmissionAssessmentRepository.apply(teacherId,subject.id,name.getText(),maximum.getText(),newWeight.getText(),existing,selected,reason.getText());
            JOptionPane.showMessageDialog(this,"Assessment created and "+selected.size()+" selected grades recalculated. Reload the dashboard to refresh in-memory grades.");
            try{OLARepository.OLAData data=OLARepository.loadAllOLA();AcademicData.loadDatabaseOLA(data.assessments,data.scores);}catch(Exception refresh){JOptionPane.showMessageDialog(this,"Database saved; OLA cache refresh warning: "+refresh.getMessage());}
            name.setText("");reason.setText("");loadSelected();
        }catch(Exception e){error(e);}
    }
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Post-submission assessment",JOptionPane.ERROR_MESSAGE);}
}
