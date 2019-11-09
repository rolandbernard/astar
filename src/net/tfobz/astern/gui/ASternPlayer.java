package net.tfobz.astern.gui;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;

import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.Color;
import net.tfobz.astern.AStern;

@SuppressWarnings("serial")
public class ASternPlayer extends JPanel {
	
	private JTextField txtDistbias;
	private JSlider sldTimeWait;
	private JTextArea lblStatus;
	
	private AStern as = null;
	private ASternGui asGui = null;

	private volatile boolean running = false;
	
	public ASternPlayer(ASternGui gui, AStern as) {
		this.as = as;
		this.asGui = gui;
		
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		this.setLayout(gbl_panel);
		
		// calculate button
		JButton btnCalculate = new JButton("Calculate");
		btnCalculate.addActionListener(e->{
			if(this.running == false) {
				this.running = true;
				try {
					double distBias = Double.parseDouble(this.txtDistbias.getText());
					if(distBias < 0)
						throw new NumberFormatException(); // Just to get into the right catch
					this.as.setDistBias(distBias);
					this.as.setWaitTime(0);
					AStern.Node[] sel = this.asGui.getSelection();
					List<AStern.Node> path = this.as.getPathFromTo(sel[0], sel[1]);
					double cost = this.as.getPathCost(path);
					this.lblStatus.setText("The path costs " + ((int)(cost*10000)/10000.0));
					this.as.setWaitTime((int)Math.pow(10,this.sldTimeWait.getValue()/250));
				} catch (NumberFormatException ex) {
					this.lblStatus.setText("NO LEGAL DIST BIAS");
				} catch (IllegalArgumentException | IllegalStateException ex) {
					this.lblStatus.setText(ex.getMessage().toUpperCase());
				} 
				this.running = false;
			}
		});
		GridBagConstraints gbc_btnCalculate = new GridBagConstraints();
		gbc_btnCalculate.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCalculate.insets = new Insets(0, 0, 5, 0);
		gbc_btnCalculate.gridx = 1;
		gbc_btnCalculate.gridy = 0;
		this.add(btnCalculate, gbc_btnCalculate);
		
		// animate button
		JButton btnAnimate = new JButton("Animate");
		btnAnimate.addActionListener(e->{
			new Thread(()->{
				if(this.running == false) {
					this.running = true;
					this.lblStatus.setText("Calculating...");
					try {
						double distBias = Double.parseDouble(this.txtDistbias.getText());
						if(distBias < 0)
							throw new NumberFormatException(); // Just to get into the right catch
						this.as.setDistBias(distBias);
						AStern.Node[] sel = this.asGui.getSelection();
						List<AStern.Node> path = this.as.getPathFromTo(sel[0], sel[1]);
						double cost = this.as.getPathCost(path);
						this.lblStatus.setText("The path costs " + ((int)(cost*10000)/10000.0));
					} catch (NumberFormatException ex) {
						this.lblStatus.setText("NO LEGAL DIST BIAS");
					} catch (IllegalArgumentException | IllegalStateException ex) {
						this.lblStatus.setText(ex.getMessage().toUpperCase());
					} 
					this.running = false;
				}
			}).start();
		});
		GridBagConstraints gbc_btnAnimate = new GridBagConstraints();
		gbc_btnAnimate.insets = new Insets(0, 0, 5, 0);
		gbc_btnAnimate.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAnimate.gridx = 1;
		gbc_btnAnimate.gridy = 1;
		this.add(btnAnimate, gbc_btnAnimate);
		
		// dist bias label
		JLabel lblDistanceWeight = new JLabel("Dist bias");
		GridBagConstraints gbc_lblDistanceWeight = new GridBagConstraints();
		gbc_lblDistanceWeight.insets = new Insets(0, 0, 5, 5);
		gbc_lblDistanceWeight.anchor = GridBagConstraints.EAST;
		gbc_lblDistanceWeight.gridx = 0;
		gbc_lblDistanceWeight.gridy = 3;
		this.add(lblDistanceWeight, gbc_lblDistanceWeight);
		
		// dist bias text field
		txtDistbias = new JTextField();
		txtDistbias.setText("1.0");
		GridBagConstraints gbc_txtDistbias = new GridBagConstraints();
		gbc_txtDistbias.insets = new Insets(0, 0, 5, 0);
		gbc_txtDistbias.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDistbias.gridx = 1;
		gbc_txtDistbias.gridy = 3;
		this.add(txtDistbias, gbc_txtDistbias);
		txtDistbias.setColumns(10);

		sldTimeWait = new JSlider();
		sldTimeWait.setMinimum(0);
		sldTimeWait.setMaximum(1000);
		sldTimeWait.setValue(500);
		this.as.setWaitTime(100);
		sldTimeWait.setToolTipText("Fast..Slow");
		sldTimeWait.addChangeListener(e->{
			this.as.setWaitTime((int)Math.pow(10,this.sldTimeWait.getValue()/250));
		});
		GridBagConstraints gbc_sldWait = new GridBagConstraints();
		gbc_sldWait.fill = GridBagConstraints.HORIZONTAL;
		gbc_sldWait.gridwidth = 2;
		gbc_sldWait.gridx = 0;
		gbc_sldWait.gridy = 4;
		this.add(sldTimeWait, gbc_sldWait);
		
		// result/status text area
		lblStatus = new JTextArea();
		lblStatus.setEditable(false);
		lblStatus.setBackground(this.getBackground());
		lblStatus.setForeground(Color.ORANGE);
		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
		gbc_lblStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblStatus.gridwidth = 2;
		gbc_lblStatus.gridx = 0;
		gbc_lblStatus.gridy = 6;
		this.add(lblStatus, gbc_lblStatus);
	}
}
