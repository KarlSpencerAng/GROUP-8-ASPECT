package studentmanagement;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/** Presentation-only shell: preserves the dashboard tabs and their listeners. */
public final class AspectDashboardShell {
    private static final Color MAROON = new Color(119, 0, 0);
    private static final Color GOLD = new Color(234, 181, 44);
    private static final Color PALE = new Color(249, 248, 246);

    private AspectDashboardShell() { }

    private static BufferedImage loadLogo() {
        String[] resources = {
            "aspect-logo (1).png", "aspect-logo.png",
            "/studentmanagement/aspect-logo (1).png",
            "/studentmanagement/aspect-logo.png",
            "/aspect-logo (1).png", "/aspect-logo.png"
        };
        for (String name : resources) {
            try {
                URL url = AspectDashboardShell.class.getResource(name);
                if (url != null) {
                    BufferedImage image = ImageIO.read(url);
                    if (image != null) return image;
                }
            } catch (IOException ex) {
                System.err.println("Cannot read logo " + name + ": " + ex.getMessage());
            }
        }
        String[] paths = {
            "src/studentmanagement/aspect-logo (1).png",
            "src/studentmanagement/aspect-logo.png",
            "studentmanagement/aspect-logo (1).png",
            "studentmanagement/aspect-logo.png",
            "src/aspect-logo (1).png", "src/aspect-logo.png"
        };
        for (String path : paths) {
            try {
                File file = new File(path);
                if (file.isFile()) {
                    BufferedImage image = ImageIO.read(file);
                    if (image != null) return image;
                }
            } catch (IOException ex) {
                System.err.println("Cannot read logo " + path + ": " + ex.getMessage());
            }
        }
        System.err.println("ASPECT logo not found. Place aspect-logo (1).png or aspect-logo.png in src/studentmanagement.");
        return null;
    }

    public static void install(JFrame frame, JTabbedPane tabs, String role) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAROON);
        header.setPreferredSize(new Dimension(100, 140));

        JLabel branding = new JLabel();
        BufferedImage logo = loadLogo();
        if (logo != null) {
            // Change this width to resize the logo. Height follows its original aspect ratio.
            int logoWidth = 300;
            int logoHeight = Math.max(1, (int) Math.round(
                    logo.getHeight() * ((double) logoWidth / logo.getWidth())));
            // Constrain the height so the logo fits within the 140-pixel header.
            if (logoHeight > 115) {
                logoHeight = 115;
                logoWidth = Math.max(1, (int) Math.round(
                        logo.getWidth() * ((double) logoHeight / logo.getHeight())));
            }
            branding.setIcon(new ImageIcon(logo.getScaledInstance(
                    logoWidth, logoHeight, Image.SCALE_SMOOTH)));
        } else {
            branding.setText("ASPECT");
            branding.setForeground(Color.WHITE);
            branding.setFont(new Font("SansSerif", Font.BOLD, 28));
        }
        branding.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        header.add(branding, BorderLayout.WEST);

        JLabel roleLabel = new JLabel(role + " Portal   ");
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.add(roleLabel, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout());
        top.add(header, BorderLayout.CENTER);
        JPanel goldLine = new JPanel();
        goldLine.setBackground(GOLD);
        goldLine.setPreferredSize(new Dimension(1, 5));
        top.add(goldLine, BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH);

        tabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected int calculateTabAreaHeight(int placement, int runs, int maxHeight) {
                return 0;
            }
            @Override
            protected void paintTabArea(Graphics graphics, int placement, int selectedIndex) { }
        });
        tabs.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tabs.setBackground(Color.WHITE);

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(PALE);
        nav.setBorder(BorderFactory.createEmptyBorder(14, 9, 14, 9));

        JLabel menu = new JLabel("  " + role.toUpperCase() + " MENU");
        menu.setForeground(MAROON);
        menu.setFont(new Font("SansSerif", Font.BOLD, 14));
        menu.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(menu);
        nav.add(Box.createVerticalStrut(13));

        final JButton[] buttons = new JButton[tabs.getTabCount()];
        for (int i = 0; i < tabs.getTabCount(); i++) {
            final int index = i;
            JButton button = new JButton(tabs.getTitleAt(i));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 39));
            button.setPreferredSize(new Dimension(235, 39));
            button.setFont(new Font("SansSerif", Font.PLAIN, 12));
            button.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 5));
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.addActionListener(event -> tabs.setSelectedIndex(index));
            buttons[i] = button;
            nav.add(button);
            nav.add(Box.createVerticalStrut(3));
        }

        Runnable update = () -> {
            for (int i = 0; i < buttons.length; i++) {
                boolean selected = tabs.getSelectedIndex() == i;
                buttons[i].setBackground(selected ? MAROON : PALE);
                buttons[i].setForeground(selected ? Color.WHITE : new Color(40, 40, 40));
            }
        };
        tabs.addChangeListener(event -> update.run());
        update.run();

        JScrollPane navScroll = new JScrollPane(nav);
        navScroll.setPreferredSize(new Dimension(248, 1));
        navScroll.setBorder(BorderFactory.createMatteBorder(
                0, 0, 0, 1, new Color(222, 222, 222)));
        navScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel sidebar = new JPanel(new BorderLayout());
        JButton logoutButton = new JButton("Sign Out");
        logoutButton.setBackground(MAROON);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(event -> {
            int confirm = JOptionPane.showConfirmDialog(
                    frame, "Are you sure you want to sign out?",
                    "Confirm Sign Out", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new Login().setVisible(true);
                frame.dispose();
            }
        });

        JPanel logoutPanel = new JPanel(new BorderLayout());
        logoutPanel.setBackground(PALE);
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));
        logoutPanel.add(logoutButton, BorderLayout.CENTER);
        sidebar.add(navScroll, BorderLayout.CENTER);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);
        root.add(sidebar, BorderLayout.WEST);
        root.add(tabs, BorderLayout.CENTER);

        // Essential: display the constructed shell instead of leaving the old content pane.
        frame.setContentPane(root);
        frame.revalidate();
        frame.repaint();
    }
}
