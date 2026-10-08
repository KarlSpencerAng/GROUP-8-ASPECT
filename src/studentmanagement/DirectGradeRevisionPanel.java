package studentmanagement;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Direct revisions are separate from student-initiated concern handling. */
public final class DirectGradeRevisionPanel extends JPanel {
    private final int teacherId;
    private final JComboBox<DirectGradeRevisionRepository.Entry> choices=new JComboBox<>();
    private final JTextField[] fields=new JTextField[5];
    private final JTextField ola=new JTextField(8);
    private final JTextArea reason=new JTextArea(3,30);
    private final JLabel info=new JLabel("Select a submitted grade.");
    public DirectGradeRevisionPanel(int teacherId){
        this.teacherId=teacherId;
        setLayout(new BorderLayout(10,10));setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        JPanel top=new JPanel(new BorderLayout(8,8));
        top.add(new JLabel("Assigned teacher's submitted grades:"),BorderLayout.NORTH);
        top.add(choices,BorderLayout.CENTER);
        JButton refresh=new JButton("Refresh submitted grades");top.add(refresh,BorderLayout.EAST);
        add(top,BorderLayout.NORTH);
        JPanel form=new JPanel(new GridLayout(7,2,8,8));
        String[] labels={"CO1","CO2","CO3","Final exam","Coursera"};
        for(int i=0;i<5;i++){form.add(new JLabel(labels[i]+" (0–100)"));fields[i]=new JTextField(8);form.add(fields[i]);}
        form.add(new JLabel("OLA (calculated, read-only)"));ola.setEditable(false);form.add(ola);
        form.add(new JLabel("Required revision reason"));form.add(new JScrollPane(reason));
        add(form,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(8,8));
        bottom.add(info,BorderLayout.NORTH);
        JButton save=new JButton("Confirm direct grade revision");bottom.add(save,BorderLayout.SOUTH);add(bottom,BorderLayout.SOUTH);
        choices.addActionListener(e->showSelection());
        refresh.addActionListener(e->refresh());
        save.addActionListener(e->save());
        refresh();
    }
    private void showSelection(){
        DirectGradeRevisionRepository.Entry row=(DirectGradeRevisionRepository.Entry)choices.getSelectedItem();
        if(row==null)return;
        for(int i=0;i<4;i++)fields[i].setText(row.scores[i].toPlainString());
        fields[4].setText(row.scores[5].toPlainString());ola.setText(row.scores[4].toPlainString());
        reason.setText("");info.setText("Original values loaded. OLA is controlled by assessments.");
    }
    private void refresh(){
        try{
            List<DirectGradeRevisionRepository.Entry> rows=DirectGradeRevisionRepository.list(teacherId);
            choices.removeAllItems();for(DirectGradeRevisionRepository.Entry r:rows)choices.addItem(r);
            if(rows.isEmpty())info.setText("No submitted grades found for your assigned subjects.");
        }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Load error",JOptionPane.ERROR_MESSAGE);}
    }
    private void save(){
        DirectGradeRevisionRepository.Entry row=(DirectGradeRevisionRepository.Entry)choices.getSelectedItem();
        if(row==null){JOptionPane.showMessageDialog(this,"Select a submitted grade first.");return;}
        String[] inputs=new String[5];for(int i=0;i<5;i++)inputs[i]=fields[i].getText();
        if(JOptionPane.showConfirmDialog(this,"Revise "+row.student+" in "+row.subject+"? This action is audited.","Confirm revision",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{
            DirectGradeRevisionRepository.revise(teacherId,row.gradeId,inputs,reason.getText());
            JOptionPane.showMessageDialog(this,"Revision saved with an audit record. Refresh Grade Management to view updated grades.");
            refresh();
        }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Revision not saved",JOptionPane.ERROR_MESSAGE);}
    }
}
