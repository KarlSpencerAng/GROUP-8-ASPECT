package studentmanagement;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public final class StudentEnrollmentPanel extends JPanel {
    private final int studentId;
    private final JComboBox<EnrollmentRequestRepository.Subject> subjects=new JComboBox<>();
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"Request ID","Subject","Status"},0){public boolean isCellEditable(int r,int c){return false;}};
    public StudentEnrollmentPanel(int studentId){
        this.studentId=studentId;setLayout(new BorderLayout(10,10));setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        JPanel top=new JPanel(new BorderLayout(8,8));top.add(new JLabel("Available subjects (assigned teacher shown):"),BorderLayout.NORTH);top.add(subjects,BorderLayout.CENTER);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton request=new JButton("Request Enrollment"),refresh=new JButton("Refresh");buttons.add(request);buttons.add(refresh);top.add(buttons,BorderLayout.SOUTH);add(top,BorderLayout.NORTH);
        add(new JScrollPane(new JTable(model)),BorderLayout.CENTER);
        request.addActionListener(e->{EnrollmentRequestRepository.Subject s=(EnrollmentRequestRepository.Subject)subjects.getSelectedItem();if(s==null)return;try{EnrollmentRequestRepository.request(studentId,s.id);reload();JOptionPane.showMessageDialog(this,"Enrollment request submitted. Await teacher approval.");}catch(SQLException ex){error(ex);}});
        refresh.addActionListener(e->reload());reload();
    }
    private void reload(){try{subjects.removeAllItems();for(EnrollmentRequestRepository.Subject s:EnrollmentRequestRepository.availableSubjects())subjects.addItem(s);model.setRowCount(0);for(EnrollmentRequestRepository.Row r:EnrollmentRequestRepository.studentRequests(studentId))model.addRow(new Object[]{r.id,r.subject,r.status});}catch(SQLException ex){error(ex);}}
    private void error(SQLException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Enrollment Error",JOptionPane.ERROR_MESSAGE);}
}
