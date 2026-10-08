/**
 * CurriculumPanel.java
 * Purpose: Curriculum interface.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Curriculum Year 2025, IT. Course data transcribed from supplied MyMapua screenshots.
 *  Completion is linked to released grades only; historical credits cannot be inferred
 *  from the current in-memory grade records. */
public class CurriculumPanel extends JPanel {
    private static class Entry {
        final int year, term; final String code, title, lecture, lab, prerequisite, corequisite;
        final int units;
        Entry(int y,int t,String c,String n,String le,String la,int u,String pre,String co) {
            year=y;term=t;code=c;title=n;lecture=le;lab=la;units=u;prerequisite=pre;corequisite=co;
        }
    }
    private static final List<Entry> CORE = new ArrayList<>();
    private static final List<Entry> ELECTIVES = new ArrayList<>();
    private static void add(int y,int t,String code,String title,String lecture,String lab,int units,String pre) {
        CORE.add(new Entry(y,t,code,title,lecture,lab,units,pre,""));
    }
    private static void el(String code,String title,String pre) {
        ELECTIVES.add(new Entry(3,4,code,title,"4.5","-",3,pre,""));
    }
    static {
        add(1,1,"CSS121P","COMPUTER PROGRAMMING 1","2.3","4.5",3,"");
        add(1,1,"FW01-2","PHYSICAL ACTIVITIES TOWARD HEALTH AND FITNESS 1 (PATHFIT 1): MOVEMENT COMPETENCY TRAINING","3","-",2,"");
        add(1,1,"GED101","UNDERSTANDING THE SELF","4.5","-",3,"");
        add(1,1,"GED103","READINGS IN PHILIPPINE HISTORY","4.5","-",3,"");
        add(1,1,"ITS100","INTRODUCTION TO INFORMATION TECHNOLOGY","4.5","-",3,"");
        add(1,1,"MATH165","COLLEGE ALGEBRA WITH ANALYTIC GEOMETRY","4.5","-",3,"");
        add(1,1,"NSTP001","NATIONAL SERVICE TRAINING PROGRAM GENERAL MODULE","-","4.5",2,"");
        add(1,2,"CSS122P","COMPUTER PROGRAMMING 2","2.3","4.5",3,"CSS121P");
        add(1,2,"CWT5001","CIVIC WELFARE TRAINING SERVICE 1","-","4.5",2,"NSTP001");
        add(1,2,"FW02-2","PHYSICAL ACTIVITIES TOWARD HEALTH AND FITNESS 2 (PATHFIT 2): EXERCISE-BASED FITNESS ACTIVITIES","3","-",2,"FW01-2");
        add(1,2,"GED104","SCIENCE, TECHNOLOGY AND SOCIETY","4.5","-",3,"");
        add(1,2,"GED105","THE CONTEMPORARY WORLD","4.5","-",3,"");
        add(1,2,"ITS110P","COMPUTER HARDWARE FUNDAMENTALS","2.3","4.5",3,"");
        add(1,2,"ITS121-1L","WEB SYSTEMS AND TECHNOLOGIES 1 LABORATORY","-","4.5",1,"CSS121P");
        add(1,2,"MATH170","LINEAR ALGEBRA WITH COMPUTER APPLICATIONS","4.5","-",3,"MATH165");
        add(1,3,"CSS130-1","DATA STRUCTURES AND ALGORITHMS","4.5","-",3,"CSS121P");
        add(1,3,"CWT5002","CIVIC WELFARE TRAINING SERVICE 2","-","4.5",2,"CWT5001");
        add(1,3,"FW03-2","PHYSICAL ACTIVITIES TOWARD HEALTH AND FITNESS 3 (PATHFIT 3): DANCE / MARTIAL ARTS","3","-",2,"FW02-2");
        add(1,3,"GED106","PURPOSIVE COMMUNICATION","4.5","-",3,"");
        add(1,3,"GED107","ETHICS","4.5","-",3,"");
        add(1,3,"ITS131P","INFORMATION MANAGEMENT","3","4.5",3,"CSS122P");
        add(1,3,"ITS161-1L","DATA COMMUNICATION AND NETWORKING FUNDAMENTALS","-","9",2,"CSS121P");
        add(1,3,"MATH174","DIFFERENTIAL AND INTEGRAL CALCULUS","4.5","-",3,"MATH165");
        add(2,1,"CSS123P","COMPUTER PROGRAMMING 3","3","4.5",3,"ITS131P");
        add(2,1,"GED108","ART APPRECIATION","4.5","-",3,"");
        add(2,1,"ISS120","INFORMATION SYSTEMS AND BUSINESS PROCESSES","4.5","-",3,"ITS100");
        add(2,1,"ITS162-1L","DATA COMMUNICATION AND NETWORKING ESSENTIALS","-","9",2,"ITS161-1L");
        add(2,1,"MATH181","QUANTITATIVE METHODS","4.5","-",3,"MATH170");
        add(2,1,"PCC150","PROFESSIONAL COMMUNICATIONS COURSE","4.5","-",3,"");
        add(2,2,"ENV121","ENVIRONMENTAL SCIENCE AND SUSTAINABILITY","4.5","-",3,"2ND YEAR STANDING");
        add(2,2,"GED102","MATHEMATICS IN THE MODERN WORLD","4.5","-",3,"");
        add(2,2,"GED110","PEOPLE AND EARTH'S ECOSYSTEM","4.5","-",3,"");
        add(2,2,"GEE130","GE ELECTIVE","4.5","-",3,"");
        add(2,2,"ITS163-1L","DATA COMMUNICATION AND NETWORKING CORE","-","9",2,"ITS162-1L");
        add(2,3,"CSS131-1","DISCRETE MATHEMATICS 1","4.5","-",3,"CSS130-1");
        add(2,3,"DSS110","INTRODUCTION TO DATA SCIENCE","4.5","-",3,"ITS131P");
        add(2,3,"FW04-2","PHYSICAL ACTIVITIES TOWARD HEALTH AND FITNESS 4 (PATHFIT 4): GROUP EXERCISE","3","-",2,"FW03-2");
        add(2,3,"GEE120","GE ELECTIVE","4.5","-",3,"");
        add(2,3,"ITS112P","COMPUTER ARCHITECTURE AND ORGANIZATION","3","4.5",3,"ITS110P");
        add(2,4,"ITS122P","WEB SYSTEMS AND TECHNOLOGIES 2","3","4.5",3,"ITS121-1L, ITS131P");
        add(2,4,"ITS132P","DATA WAREHOUSING AND DATA MINING","3","4.5",3,"ITS131P");
        add(2,4,"ITS141-1","HUMAN-COMPUTER INTERACTION 1","4.5","-",3,"CSS123P");
        add(2,4,"ITS151P","SYSTEMS INTEGRATION AND ARCHITECTURE 1","3","4.5",3,"CSS123P, ISS120");
        add(3,1,"CSS140-1","ARTIFICIAL INTELLIGENCE","4.5","-",3,"CSS130-1");
        add(3,1,"ITS120P","APPLICATION DEVELOPMENT AND EMERGING TECHNOLOGIES","3","4.5",3,"CSS123P");
        add(3,1,"ITS142P","HUMAN COMPUTER INTERACTION 2","3","4.5",3,"ITS141-1");
        add(3,1,"ITS152P","SYSTEMS INTEGRATION AND ARCHITECTURE 2","3","4.5",3,"ITS151P");
        add(3,1,"ITS165-1","INFORMATION SECURITY AND ASSURANCE 1","4.5","-",3,"ITS131P, ITS161-1L");
        add(3,2,"ISS160","PROJECT MANAGEMENT","4.5","-",3,"CSS123P");
        add(3,2,"ITS150P","OPERATING SYSTEMS","3","4.5",3,"ITS112P");
        add(3,2,"ITS166-1","INFORMATION SECURITY AND ASSURANCE 2","3","4.5",3,"ITS165-1");
        add(3,2,"RZL110","THE LIFE AND WORKS OF RIZAL","4.5","-",3,"");
        add(3,3,"CSS153P","SOFTWARE QUALITY","3","4.5",3,"ITS142P");
        add(3,3,"IE103-2","TECHNOPRENEURSHIP 101","4.5","-",3,"");
        add(3,3,"ITS109-1","RESEARCH METHODS IN INFORMATION TECHNOLOGY","4.5","-",3,"ITS152P");
        add(3,3,"ITS153P","SYSTEMS ADMINISTRATION AND MAINTENANCE","3","4.5",3,"ITS152P, ITS163-1L");
        add(3,4,"ITS105-1","SOCIAL AND PROFESSIONAL ISSUES","4.5","-",3,"CSS123P, GED107");
        add(3,4,"ITS199-1R","PRACTICUM 1","4.5","-",3,"3RD YEAR STANDING");
        add(3,4,"ITS200-01","THESIS 1","1.5","-",1,"ITS109-1");
        add(4,1,"ITS198F","CAREER DEVELOPMENT AND SEMINAR IN IT","-","4.5",1,"FOR GRADUATING STUDENTS ONLY");
        add(4,1,"ITS199-2R","PRACTICUM 2","4.5","-",3,"ITS199-1R");
        add(4,1,"ITS200-02","THESIS 2","4.5","-",1,"ITS200-01");
        add(4,1,"SGE100X","STUDENT GLOBAL EXPERIENCE","-","-",0,"4TH YEAR STANDING");
        el("CSS171-1","GRAPHICS AND VISUAL COMPUTING","CSS123P");
        el("CSS172-1","PATTERN RECOGNITION","CSS123P");
        el("ECS176-1","INTRODUCTION TO GAME PROGRAMMING","CSS123P");
        el("ISS171-1","SUPPLY CHAIN MANAGEMENT","ITS122P");
        el("ISS172-1","CUSTOMER RELATION MANAGEMENT","ITS131P");
        el("ISS173-1","ESSENTIAL OF SAS","CSS123P");
        el("ISS174-1","IT AUDIT AND CONTROL","ITS131P");
        el("ITS170-1","IT INFRASTRUCTURE LIBRARY FOUNDATION COURSE","ITS131P");
        el("ITS171-1","FUNDAMENTALS OF SAP","ITS131P");
        el("ITS172-1","MOBILE APPLICATION DEVELOPMENT","CSS123P");
        el("ITS173-1","EMBEDDED SYSTEMS","CSS123P");
        el("ITS174-1","INTERNET OF THINGS","CSS123P");
        el("ITS175-1","CLOUD COMPUTING","CSS123P");
        el("ITS176","BLOCKCHAIN TECHNOLOGY","CSS123P");
    }

    // Catalog is shared with the teacher-only historical-record editor.
    public static java.util.List<String> curriculumOptions() {
        java.util.List<String> result = new ArrayList<>();
        for (Entry e : CORE) result.add(e.code + " - " + e.title);
        for (Entry e : ELECTIVES) result.add(e.code + " - " + e.title + " [elective]");
        return result;
    }
    public static int curriculumUnits(String code) {
        for (Entry e : CORE) if (e.code.equals(code)) return e.units;
        for (Entry e : ELECTIVES) if (e.code.equals(code)) return e.units;
        throw new IllegalArgumentException("Course not found in curriculum: " + code);
    }
    public static boolean isElective(String code) {
        for (Entry e : ELECTIVES) if (e.code.equals(code)) return true;
        return false;
    }

    private static Color colorFor(String state) {
        switch (state) {
            case "Taken": return new Color(22, 62, 164);
            case "In Current Load": return new Color(20, 115, 59);
            case "Incomplete": return new Color(177, 34, 34);
            case "Exempted/Credited": return new Color(150, 90, 0);
            case "Failed": return new Color(160, 55, 40);
            default: return new Color(65, 65, 65);
        }
    }

    private static JLabel legendItem(String label) {
        JLabel item = new JLabel(label);
        item.setForeground(colorFor(label));
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 212)),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        return item;
    }

    private final Student student;
    private final JLabel summary = new JLabel();
    private final JPanel tables = new JPanel();
    private final JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
    private final JComboBox<String> category = new JComboBox<>(new String[]{"Core Courses","Electives","Specialization"});
    private final JComboBox<String> yearFilter = new JComboBox<>(new String[]{"All years","Year 1","Year 2","Year 3","Year 4"});

    public CurriculumPanel(Student student) {
        this.student=student;
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        JPanel top=new JPanel(new BorderLayout(6,6));
        JLabel heading=new JLabel("My Curriculum — Information Technology | Curriculum Year: 2025 | Specialization: Unassigned");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD,16f));
        top.add(heading,BorderLayout.NORTH);
        JPanel controls=new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Category:"));controls.add(category);
        controls.add(new JLabel("Taken year:"));controls.add(yearFilter);
        JButton refresh=new JButton("Refresh Curriculum");controls.add(refresh);
        top.add(controls,BorderLayout.CENTER);
        JPanel statusPanel = new JPanel(new BorderLayout());
        legend.add(new JLabel("Legend:"));
        for (String label : new String[]{"Taken", "In Current Load", "Pending Enrollment", "Not Yet Taken", "Incomplete", "Exempted/Credited", "Failed"}) {
            legend.add(legendItem(label));
        }
        statusPanel.add(legend, BorderLayout.NORTH);
        statusPanel.add(summary, BorderLayout.SOUTH);
        top.add(statusPanel,BorderLayout.SOUTH);
        add(top,BorderLayout.NORTH);
        tables.setLayout(new BoxLayout(tables,BoxLayout.Y_AXIS));
        add(new JScrollPane(tables),BorderLayout.CENTER);
        JLabel note=new JLabel("Screenshot target: 187 units; transcribed core (168) + IT electives (6) = 174. Unmapped: 13 units; verify curriculum before official totals.");
        note.setFont(note.getFont().deriveFont(11f));
        add(note,BorderLayout.SOUTH);
        category.addActionListener(e->refresh());
        yearFilter.addActionListener(e->refresh());
        refresh.addActionListener(e->refresh());
        refresh();
    }
    private java.util.Map<String,String> enrollmentStatuses = java.util.Collections.emptyMap();
    private String status(Entry entry) {
        // Current enrollment and released grades take precedence over historical records.
        for (Grade grade : AcademicData.getGradesByStudent(student.getStudentNumber())) {
            if (!entry.code.equals(grade.getCourse().getCourseCode())) continue;
            if ("Draft".equals(grade.getStatus())) return "In Current Load";
            if ("Posted".equals(grade.getStatus())) {
                if (!grade.isCalculationComplete()) return "In Current Load";
                return "PASSED".equals(grade.getAcademicResult()) ? "Taken" : "Failed";
            }
            return "In Current Load";
        }
        AcademicData.HistoricalRecord record = AcademicData.getHistoricalRecord(student.getStudentNumber(), entry.code);
        if (record != null) return record.getStatus();
        String enrollmentStatus = enrollmentStatuses.get(entry.code);
        if (enrollmentStatus != null) return enrollmentStatus;
        return "Not Yet Taken";
    }
    private int[] unitProgress() {
        int corePassed=0, coreExempt=0, electivePassed=0, electiveExempt=0;
        for (Entry e : CORE) {
            String state=status(e);
            if ("Taken".equals(state)) corePassed+=e.units;
            if ("Exempted/Credited".equals(state)) coreExempt+=e.units;
        }
        for (Entry e : ELECTIVES) {
            String state=status(e);
            if ("Taken".equals(state)) electivePassed+=e.units;
            if ("Exempted/Credited".equals(state)) electiveExempt+=e.units;
        }
        int electiveCredited=Math.min(6,electivePassed+electiveExempt);
        int electivePassedCount=Math.min(electivePassed,electiveCredited);
        int passed=corePassed+electivePassedCount;
        int exempt=coreExempt+electiveCredited-electivePassedCount;
        int credited=Math.min(187,passed+exempt);
        return new int[]{passed,exempt,credited,Math.max(0,187-credited)};
    }

    private void refresh() {
        try { enrollmentStatuses = EnrollmentRequestRepository.curriculumStatuses(student.getStudentId()); }
        catch (java.sql.SQLException ex) { enrollmentStatuses = java.util.Collections.emptyMap(); System.err.println("Unable to load enrollment statuses: " + ex.getMessage()); }
        tables.removeAll();
        int[] progress=unitProgress();
        String overall="Recorded progress (187-unit screenshot target; 13 units unmapped): Passed " + progress[0] + " | Exempted/Credited " + progress[1] + " | Credited " + progress[2] + " | Uncredited vs screenshot " + progress[3] + " units (includes unmapped requirements). ";
        int chosen=category.getSelectedIndex();
        List<Entry> entries=chosen==0?CORE:ELECTIVES;
        if (chosen==2) {
            JLabel empty=new JLabel("There are no Specialization Curriculum courses (Unassigned).");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);tables.add(empty);
        } else {
            int selectedYear=yearFilter.getSelectedIndex();
            int taken=0, current=0, incomplete=0, credited=0;
            for (Entry entry:entries) {
                if (selectedYear!=0 && entry.year!=selectedYear) continue;
                String state=status(entry);
                if (state.equals("Taken")) taken+=entry.units;
                if (state.equals("In Current Load")) current+=entry.units;
                if (state.equals("Incomplete")) incomplete+=entry.units;
                if (state.equals("Exempted/Credited")) credited+=entry.units;
            }
            for (int year=1;year<=4;year++) {
                if (selectedYear!=0 && selectedYear!=year) continue;
                for (int term=1;term<=4;term++) {
                    final int y=year,t=term;
                    List<Entry> group=new ArrayList<>();
                    for (Entry e:entries) if(e.year==y && e.term==t) group.add(e);
                    if (group.isEmpty()) continue;
                    JPanel section=new JPanel(new BorderLayout(0,5));
                    section.setAlignmentX(Component.LEFT_ALIGNMENT);
                    section.setBorder(BorderFactory.createEmptyBorder(5,0,13,0));
                    JLabel label=new JLabel("Year "+year+" — Term "+term);
                    label.setFont(label.getFont().deriveFont(Font.BOLD,14f));
                    section.add(label,BorderLayout.NORTH);
                    String[] cols={"Course Code","Course Title","Lec Hrs","Lab Hrs","Units","Pre-Requisites","Co-Requisites","Status"};
                    DefaultTableModel model=new DefaultTableModel(cols,0){
                        @Override public boolean isCellEditable(int r,int c){return false;}
                    };
                    int units=0;
                    for(Entry e:group) {
                        units+=e.units;
                        model.addRow(new Object[]{e.code,e.title,e.lecture,e.lab,e.units,e.prerequisite,e.corequisite,status(e)});
                    }
                    JTable table=new JTable(model);
                    DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
                        @Override public Component getTableCellRendererComponent(JTable t, Object value,
                                boolean selected, boolean focus, int row, int column) {
                            Component cell = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                            String state = String.valueOf(t.getModel().getValueAt(t.convertRowIndexToModel(row), 7));
                            if (!selected) {
                                cell.setForeground(colorFor(state));
                                cell.setBackground(row % 2 == 0 ? Color.WHITE : new Color(247, 249, 253));
                            }
                            return cell;
                        }
                    };
                    for (int c = 0; c < model.getColumnCount(); c++) {
                        table.getColumnModel().getColumn(c).setCellRenderer(statusRenderer);
                    }
                    table.setRowHeight(28);
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    int[] widths={110,380,70,70,65,180,130,125};
                    for(int c=0;c<widths.length;c++)table.getColumnModel().getColumn(c).setPreferredWidth(widths[c]);
                    JPanel content=new JPanel(new BorderLayout());
                    content.add(table.getTableHeader(),BorderLayout.NORTH);
                    content.add(table,BorderLayout.CENTER);
                    section.add(content,BorderLayout.CENTER);
                    section.add(new JLabel("Term credit units: "+units),BorderLayout.SOUTH);
                    tables.add(section);
                }
            }
            summary.setText(overall + "Visible courses — Passed: " + taken + " units | Exempted/Credited: " + credited
                    + " | Current load: " + current + " | Incomplete: " + incomplete
                    + " (available records only)");
            if(chosen==1) summary.setText("Elective OPTIONS (6 units required; do not sum every option). " + summary.getText());
        }
        if(chosen==2) summary.setText(overall + "Specialization: Unassigned");
        tables.revalidate();tables.repaint();
    }
}
