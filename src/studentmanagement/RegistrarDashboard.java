package studentmanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/** Registrar portal for auditing, posting, and returning class grade sheets. */
public class RegistrarDashboard extends JFrame {
    private final int registrarId;
    private final DefaultTableModel submissionsModel;
    private final DefaultTableModel gradesModel;
    private final JTable submissionsTable;
    private final JTable gradesTable;
    private final JLabel analytics = new JLabel("Select a submission to audit.");

    public RegistrarDashboard(int registrarId) {
        this.registrarId=registrarId;
        setTitle("ASPECT - Registrar Portal");
        setSize(1200,760); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        submissionsModel=new DefaultTableModel(new String[]{"ID","Subject","Teacher","Status","Submitted"},0){public boolean isCellEditable(int r,int c){return false;}};
        gradesModel=new DefaultTableModel(new String[]{"Student No.","Student","CO1","CO2","CO3","Final","OLA","Coursera","Final %","Result"},0){public boolean isCellEditable(int r,int c){return false;}};
        submissionsTable=new JTable(submissionsModel); gradesTable=new JTable(gradesModel);

        JPanel pending=new JPanel(new BorderLayout(8,8));
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh=new JButton("Refresh Pending"); JButton post=new JButton("Post & Finalize"); JButton returned=new JButton("Return for Revision");
        top.add(refresh);top.add(post);top.add(returned);top.add(analytics);
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(submissionsTable),new JScrollPane(gradesTable)); split.setResizeWeight(.35);
        pending.add(top,BorderLayout.NORTH);pending.add(split,BorderLayout.CENTER);

        JTabbedPane tabs=new JTabbedPane();
        tabs.addTab("Pending Grade Submissions",pending);
        tabs.addTab("Student Account Management", new StudentRegistrationPanel(registrarId));
        AspectDashboardShell.install(this,tabs,"Registrar");

        refresh.addActionListener(e->loadPending());
        submissionsTable.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())loadSelectedSheet();});
        post.addActionListener(e->postSelected()); returned.addActionListener(e->returnSelected());
        loadPending();
    }

    private Integer selectedSubmission(){int r=submissionsTable.getSelectedRow();return r<0?null:Integer.valueOf(submissionsModel.getValueAt(r,0).toString());}
    private void loadPending(){
        try{submissionsModel.setRowCount(0);gradesModel.setRowCount(0);analytics.setText("Select a submission to audit.");
            for(RegistrarRepository.SubmissionRow r:RegistrarRepository.loadSubmissions("Pending"))
                submissionsModel.addRow(new Object[]{r.submissionId,r.subjectCode+" - "+r.subjectName,r.teacherName,r.status,r.submittedAt});
        }catch(SQLException ex){error(ex.getMessage());}
    }
    private void loadSelectedSheet(){Integer id=selectedSubmission();if(id==null)return;try{
        List<RegistrarRepository.GradeRow> rows=RegistrarRepository.loadGradeSheet(id);gradesModel.setRowCount(0);int pass=0,fail=0;double sum=0,min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
        for(RegistrarRepository.GradeRow r:rows){gradesModel.addRow(new Object[]{r.studentNumber,r.studentName,r.co1,r.co2,r.co3,r.finalExam,r.ola,r.coursera,r.finalPercentage,r.result});sum+=r.finalPercentage;min=Math.min(min,r.finalPercentage);max=Math.max(max,r.finalPercentage);if("PASS".equals(r.result))pass++;else fail++;}
        analytics.setText(rows.isEmpty()?"No pending grades found.":String.format("Students: %d | Pass: %d | Fail: %d | Avg: %.2f%% | High: %.2f%% | Low: %.2f%%",rows.size(),pass,fail,sum/rows.size(),max,min));
    }catch(SQLException ex){error(ex.getMessage());}}
    private void postSelected(){Integer id=selectedSubmission();if(id==null){error("Select a pending submission first.");return;}if(JOptionPane.showConfirmDialog(this,"Post and permanently lock this class grade sheet?","Confirm Posting",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;try{RegistrarRepository.post(id,registrarId);JOptionPane.showMessageDialog(this,"Grades posted successfully.");loadPending();}catch(Exception ex){error(ex.getMessage());}}
    private void returnSelected(){Integer id=selectedSubmission();if(id==null){error("Select a pending submission first.");return;}String reason=JOptionPane.showInputDialog(this,"Reason for returning this grade sheet:","Return for Revision",JOptionPane.WARNING_MESSAGE);if(reason==null)return;try{RegistrarRepository.returnForRevision(id,registrarId,reason);JOptionPane.showMessageDialog(this,"Grade sheet returned to the teacher.");loadPending();}catch(Exception ex){error(ex.getMessage());}}
    private void error(String m){JOptionPane.showMessageDialog(this,m,"Registrar",JOptionPane.ERROR_MESSAGE);}
}
