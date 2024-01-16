package frontend;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class AnotherPage {
    private View view;

    private JPanel panel1;
    private JTextField thisIsAnothaPageTextField;
    private JButton homePageButton;
    private JButton anotherPageButton;
    private JPanel navigation;

    private JLabel label;
    private ImageIcon img;


    public JPanel getContent() {
        return panel1;
    }

    public AnotherPage() {
        homePageButton.addActionListener(Controller.navigateHome());
        anotherPageButton.addActionListener(Controller.navigateAnother());

        try {
            img = new ImageIcon(getClass().getResource("planeIcon.png"));
            label.setIcon(img);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}