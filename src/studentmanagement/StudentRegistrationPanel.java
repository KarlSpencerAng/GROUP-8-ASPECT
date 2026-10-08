package studentmanagement;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Available only from an authenticated Registrar dashboard. */
public final class StudentRegistrationPanel extends JPanel {
    public StudentRegistrationPanel(int registrarId) {
        setLayout(new BorderLayout(12,12)); setBorder(BorderFactory.createEmptyBorder(22,22,22,22));
        JPanel fields=new JPanel(new GridLayout(4,2,10,12));
        JTextField first=new JTextField(),last=new JTextField(),number=new JTextField(),section=new JTextField();
        fields.add(new JLabel("First name"));fields.add(first);
        fields.add(new JLabel("Last name"));fields.add(last);
        fields.add(new JLabel("University student number"));fields.add(number);
        fields.add(new JLabel("Section (optional)"));fields.add(section);
        add(fields,BorderLayout.NORTH);
        JButton create=new JButton("Create Student Account"); add(create,BorderLayout.SOUTH);
        create.addActionListener(e -> {
            try {
                String username=StudentAccountRepository.createStudent(registrarId,first.getText(),last.getText(),number.getText(),section.getText());
                JOptionPane.showMessageDialog(this,"Account created. Username: "+username+"\nTemporary password: 1234\nStudent must change password at first login.");
                first.setText("");last.setText("");number.setText("");section.setText("");
            } catch(SQLException|RuntimeException ex) { JOptionPane.showMessageDialog(this,ex.getMessage(),"Unable to create account",JOptionPane.ERROR_MESSAGE); }
        });
    }
}
