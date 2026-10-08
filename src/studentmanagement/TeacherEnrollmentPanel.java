package studentmanagement;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;

public final class TeacherEnrollmentPanel extends JPanel {
    private final int teacherId;
    private final DefaultTableModel requests=new DefaultTableModel(new String[]{"Request ID","Student","Subject","Status"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable requestTable=new JTable(requests);
    private final JComboBox<Choice> students=new JComboBox<>(),subjects=new JComboBox<>();
    private final DefaultTableModel roster=new DefaultTableModel(new String[]{"Subject","Student Number","Student","Enrollment Status"},0){public boolean isCellEditable(int r,int c){return false;}};
    private static final class Choice {final int id;final String name;Choice(int i,String n){id=i;name=n;}public String toString(){return name;}}
    public TeacherEnrollmentPanel(int teacherId){this.teacherId=teacherId;setLayout(new BorderLayout(8,8));setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JTabbedPane tabs=new JTabbedPane();
        JPanel review=new JPanel(new BorderLayout(8,8));review.add(new JScrollPane(requestTable),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton approve=new JButton("Approve"),reject=new JButton("Reject"),refresh=new JButton("Refresh");actions.add(approve);actions.add(reject);actions.add(refresh);review.add(actions,BorderLayout.SOUTH);tabs.addTab("Enrollment Requests",review);
        JPanel direct=new JPanel(new BorderLayout(8,8));JPanel form=new JPanel(new GridLayout(2,2,8,8));form.add(new JLabel("Student"));form.add(students);form.add(new JLabel("My assigned subject"));form.add(subjects);direct.add(form,BorderLayout.NORTH);JButton enroll=new JButton("Enroll Student Directly");direct.add(enroll,BorderLayout.SOUTH);tabs.addTab("Direct Enrollment",direct);
        JPanel rosterPanel=new JPanel(new BorderLayout());rosterPanel.add(new JScrollPane(new JTable(roster)),BorderLayout.CENTER);JButton reloadRoster=new JButton("Refresh Class Roster");rosterPanel.add(reloadRoster,BorderLayout.SOUTH);tabs.addTab("Class Roster",rosterPanel);add(tabs,BorderLayout.CENTER);
        approve.addActionListener(e->review(true));reject.addActionListener(e->review(false));refresh.addActionListener(e->reload());reloadRoster.addActionListener(e->reload());
        enroll.addActionListener(e->{Choice st=(Choice)students.getSelectedItem(),su=(Choice)subjects.getSelectedItem();if(st==null||su==null)return;try{EnrollmentRequestRepository.directEnroll(teacherId,st.id,su.id);reload();JOptionPane.showMessageDialog(this,"Student enrolled successfully.");}catch(SQLException ex){error(ex);}});
        tabs.addChangeListener(e->reload());reload();
    }
    private void review(boolean approve){int row=requestTable.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Select a request first.");return;}int id=(Integer)requests.getValueAt(requestTable.convertRowIndexToModel(row),0);String note=JOptionPane.showInputDialog(this,approve?"Approval note (optional):":"Rejection reason (optional):","");if(note==null)return;try{EnrollmentRequestRepository.review(teacherId,id,approve,note);reload();}catch(SQLException ex){error(ex);}}
    private void reload(){try{
        requests.setRowCount(0);for(EnrollmentRequestRepository.Row r:EnrollmentRequestRepository.teacherRequests(teacherId))requests.addRow(new Object[]{r.id,r.student,r.subject,r.status});
        students.removeAllItems();subjects.removeAllItems();roster.setRowCount(0);
        try(Connection c=DatabaseConnection.getConnection()){
            try(PreparedStatement q=c.prepareStatement("SELECT UserID,StudentNumber,FirstName,LastName FROM Users WHERE Role='Student' ORDER BY LastName,FirstName");ResultSet r=q.executeQuery()){while(r.next())students.addItem(new Choice(r.getInt(1),r.getString(2)+" — "+r.getString(3)+" "+r.getString(4)));}
            try(PreparedStatement q=c.prepareStatement("SELECT SubjectID,SubjectCode,SubjectName FROM Subjects WHERE TeacherID=? ORDER BY SubjectCode")){q.setInt(1,teacherId);try(ResultSet r=q.executeQuery()){while(r.next())subjects.addItem(new Choice(r.getInt(1),r.getString(2)+" — "+r.getString(3)));}}
            String sql="SELECT s.SubjectCode,u.StudentNumber,CONCAT(u.FirstName,' ',u.LastName) StudentName,e.Status FROM Enrollments e JOIN Subjects s ON s.SubjectID=e.SubjectID JOIN Users u ON u.UserID=e.StudentID WHERE s.TeacherID=? ORDER BY s.SubjectCode,u.LastName,u.FirstName";
            try(PreparedStatement q=c.prepareStatement(sql)){q.setInt(1,teacherId);try(ResultSet r=q.executeQuery()){while(r.next())roster.addRow(new Object[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4)});}}
        }
    }catch(SQLException ex){error(ex);}}
    private void error(SQLException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Enrollment Error",JOptionPane.ERROR_MESSAGE);}
}
