package studentmanagement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Student-facing formatted Certificate of Matriculation preview and printing panel. */
public final class CertificateOfMatriculationPanel extends JPanel {
    private final String studentNumber;
    private final JEditorPane preview = new JEditorPane("text/html", "");

    public CertificateOfMatriculationPanel(String studentNumber) {
        this.studentNumber = studentNumber;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Certificate of Matriculation");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        top.add(title, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh CM");
        JButton print = new JButton("Print CM");
        refresh.addActionListener(e -> refresh());
        print.addActionListener(e -> printCM());
        buttons.add(refresh);
        buttons.add(print);
        top.add(buttons, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        preview.setEditable(false);
        preview.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        preview.setFont(new Font("Serif", Font.PLAIN, 12));
        add(new JScrollPane(preview), BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        try {
            CertificateOfMatriculationRepository.StudentInfo info =
                CertificateOfMatriculationRepository.loadStudent(studentNumber);
            List<CertificateOfMatriculationRepository.ScheduleRow> rows =
                CertificateOfMatriculationRepository.loadOfficialEnrollment(studentNumber);
            preview.setText(buildHtml(info, rows));
            preview.setCaretPosition(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Certificate of Matriculation",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private String buildHtml(CertificateOfMatriculationRepository.StudentInfo info,
                             List<CertificateOfMatriculationRepository.ScheduleRow> rows) {
        int totalUnits = 0;
        java.util.Set<String> counted = new java.util.HashSet<>();
        StringBuilder body = new StringBuilder();
        for (CertificateOfMatriculationRepository.ScheduleRow r : rows) {
            if (counted.add(r.subjectCode)) totalUnits += r.units;
            String time = r.start.isEmpty() ? "TBA" : esc(r.start) + " - " + esc(r.end);
            body.append("<tr>")
                .append(td(r.subjectCode)).append(td(r.subjectName)).append(td(r.section))
                .append(td(r.day.isEmpty() ? "TBA" : r.day)).append(td(time))
                .append(td(r.room.isEmpty() ? "TBA" : r.room)).append(td(String.valueOf(r.units)))
                .append("</tr>");
        }
        if (rows.isEmpty()) {
            body.append("<tr><td colspan='7' style='text-align:center;padding:12px'>No active enrolled subjects found.</td></tr>");
        }
        String printed = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a"));
        return "<html><head><style>" +
            "body{font-family:Arial,sans-serif;margin:28px;color:#111} h1{text-align:center;font-size:22px;margin-bottom:2px}" +
            "h2{text-align:center;font-size:16px;margin-top:2px} .meta{margin:22px 0 16px 0;line-height:1.7}" +
            "table{width:100%;border-collapse:collapse;font-size:10px} th,td{border:1px solid #333;padding:6px} th{background:#eee}" +
            ".foot{margin-top:18px;font-size:10px}.total{font-weight:bold;text-align:right;margin-top:10px}" +
            "</style></head><body>" +
            "<h1>ASPECT</h1><h2>CERTIFICATE OF MATRICULATION</h2>" +
            "<div class='meta'><b>Student Number:</b> " + esc(info.studentNumber) + "<br>" +
            "<b>Student Name:</b> " + esc(info.fullName) + "<br>" +
            "<b>Section:</b> " + esc(empty(info.section)) + "<br>" +
            "<b>Email:</b> " + esc(empty(info.email)) + "</div>" +
            "<table><tr><th>Course Code</th><th>Course Title</th><th>Section</th><th>Day</th>" +
            "<th>Time</th><th>Room</th><th>Units</th></tr>" + body + "</table>" +
            "<div class='total'>Total Units: " + totalUnits + "</div>" +
            "<div class='foot'><b>Date Printed:</b> " + esc(printed) + "<br><br>" +
            "This Certificate of Matriculation reflects the student's current active enrollments recorded in ASPECT.</div>" +
            "</body></html>";
    }

    private void printCM() {
        try {
            boolean done = preview.print(null, null, true, null, null, true);
            if (done) JOptionPane.showMessageDialog(this, "Certificate sent to the selected printer.",
                    "Certificate of Matriculation", JOptionPane.INFORMATION_MESSAGE);
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Unable to print: " + ex.getMessage(),
                    "Certificate of Matriculation", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String td(String value) { return "<td>" + esc(value) + "</td>"; }
    private static String empty(String value) { return value == null || value.trim().isEmpty() ? "N/A" : value; }
    private static String esc(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
