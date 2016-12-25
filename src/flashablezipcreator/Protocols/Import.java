/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flashablezipcreator.Protocols;

import static flashablezipcreator.AFZC.Protocols.p;
import flashablezipcreator.Core.FileNode;
import flashablezipcreator.Core.GroupNode;
import flashablezipcreator.Core.ProjectItemNode;
import flashablezipcreator.Core.ProjectNode;
import flashablezipcreator.Core.SubGroupNode;
import flashablezipcreator.DiskOperations.ReadZip;
import flashablezipcreator.MyTree;
import static flashablezipcreator.MyTree.panelLower;
import static flashablezipcreator.MyTree.progressBarFlag;
import static flashablezipcreator.MyTree.progressBarImportExport;
import flashablezipcreator.Operations.TreeOperations;
import java.awt.CardLayout;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 *
 * @author Nikhil
 */
public class Import implements Runnable {

    static ReadZip rz;
    static TreeOperations to;
    static String exisingUpdaterScript = "";
    static String fileName = "";
    static int zipType;
    public static int progressValue = 0;
    public static int fileIndex = 0;
    String path;
    ProjectItemNode rootNode;
    DefaultTreeModel model;
    public JDialog dialog;

    public Import(String path) throws IOException, ParserConfigurationException, TransformerException, SAXException, InterruptedException {
        this.rootNode = MyTree.rootNode;
        this.path = path;
        this.model = MyTree.model;
    }

    public static void fromTheZip(String path) throws ParserConfigurationException, TransformerException, IOException, SAXException {
        progressValue = 0;
        boolean containsDataXml = false;
        Xml.initialize();
        rz = new ReadZip(path);
        to = new TreeOperations();
        int maxSize = rz.filesCount;
        fileIndex = 0;
        try {
            for (Enumeration<? extends ZipEntry> e = rz.zf.entries(); e.hasMoreElements();) {
                ZipEntry ze = e.nextElement();
                String name = ze.getName();
                progressValue = (fileIndex * 100) / maxSize;
                progressBarImportExport.setValue(progressValue);
                setProgressBar("Importing " + (new File(name)).getName() + "");
                if (name.endsWith("/") || Project.getTempFilesList().contains(name)
                        || name.startsWith("META-INF")
                        || name.contains("Extract_")) {
                    System.out.println("Skipping " + name);
                    continue;
                }
                InputStream in = rz.zf.getInputStream(ze);
                if (name.equals(Xml.data_path)) {
                    Xml.fileData = rz.getStringFromFile(in);
                    containsDataXml = true;
                    System.out.println("Skipping " + name);
                    continue;
                }
                p("\ncurrent file " + name + "\n");
                String filePath = name;
                String projectName = Identify.getProjectName(name);
                int projectType = Identify.getProjectType(filePath);
                String groupName = Identify.getGroupName(filePath);
                int groupType = Identify.getGroupType(filePath);
                ArrayList<String> folderList = Identify.getFolderNames(filePath);
                String subGroupName = Identify.getSubGroupName(groupName, filePath);
                int subGroupType = groupType; //Groups that have subGroups have same type.
                String fileName = (new File(filePath)).getName();

                FileNode file = to.addFileToTree(fileName, subGroupName, subGroupType, groupName, groupType, folderList, projectName, projectType);
                file.fileSourcePath = file.path;
                rz.writeFileFromZip(in, file.fileSourcePath);
            }

            //adding nodes to tree should be done here.
            System.out.println("Terminating afzc.xml");
            Xml.terminate();
            if (containsDataXml) {
                setProgressBar("Setting file details..");
                Xml.parseXml(0); //this is to set additional details like description to nodes
                setProgressBar("Setting file details done.");
            }
            progressBarImportExport.setString("Successfully Imported");
            progressBarImportExport.setValue(100);
            JOptionPane.showMessageDialog(null, "Successfully Imported");
            progressBarImportExport.setString("0%");
            progressBarImportExport.setValue(0);
            progressBarFlag = 0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something Went Wrong!\nTry Again!");
            setCardLayout(1);
        }
    }

    public static void setProgressBar(String value) {
        switch (MyTree.progressBarFlag) {
            case 0:
                progressBarImportExport.setString(progressValue + "%");
                break;
            case 1:
                progressBarImportExport.setString(value);
                break;
            case 2:
                progressBarImportExport.setString(" ");
                break;
        }
        fileIndex++;
    }

    public static void fromZip(String path) throws IOException, ParserConfigurationException, TransformerException, SAXException {
        progressValue = 0;
        ProjectItemNode rootNode = MyTree.rootNode;
        DefaultTreeModel model = MyTree.model;
//        zipType = Identify.scanZip(path);//this will automatically detect zip type.
        boolean containsDeleteXml = false;
        boolean containsCustomXml = false;
        boolean containsDataXml = false;

        Xml.initialize();

        rz = new ReadZip(path);
        to = new TreeOperations();
        int maxSize = rz.filesCount;
        int fileIndex = 0;
        for (Enumeration<? extends ZipEntry> e = rz.zf.entries(); e.hasMoreElements();) {
            ZipEntry ze = e.nextElement();
            String name = ze.getName();
            InputStream in = rz.zf.getInputStream(ze);

            if (name.endsWith("/")) {
                continue;
            } else if (Project.getTempFilesList().contains(name)) {
                continue;
            }

            p("\ncurrent file " + name + "\n");
            String filePath = name;
            String projectName = Identify.getProjectName(name);
            int projectType = Identify.getProjectType(filePath);
            String groupName = Identify.getGroupName(filePath);
            int groupType = Identify.getGroupType(filePath);
            boolean hasSubGroup = Identify.hasSubGroup(filePath);
            String subGroupName = Identify.getSubGroupName(groupName, filePath);
            int subGroupType = groupType; //Groups that have subGroups have same type.
            String fileName = (new File(filePath)).getName();

            progressValue = (fileIndex * 100) / maxSize;
            fileIndex++;
            progressBarImportExport.setValue(progressValue);
            switch (MyTree.progressBarFlag) {
                case 0:
                    progressBarImportExport.setString(progressValue + "%");
                    break;
                case 1:
                    progressBarImportExport.setString("Importing " + fileName + "");
                    break;
                case 2:
                    progressBarImportExport.setString(" ");
                    break;
            }

            FileNode file = null;
            if (hasSubGroup) {
                //file = to.addFileToTree(fileName, subGroupName, subGroupType, groupName, groupType, projectName, projectType);
                if (subGroupType == SubGroupNode.TYPE_CUSTOM) {
                    //Xml.addFileDataToSubGroup(file);
                }
            } else {
//                if (projectType == ProjectNode.PROJECT_ROM || projectType == ProjectNode.PROJECT_GAPPS) {
//                    if (filePath.contains("META-INF/com/google/android/update-binary")) {
//                        if (to.getProjectNode(projectName, projectType) == null) {
//                            to.addChildTo(rootNode, projectName, projectType, model);
//                        }
//                        to.getProjectNode(projectName, projectType).update_binary = rz.getBytesFromFile(in);
//                        continue;
//                    } else if (filePath.contains("META-INF/com/google/android/updater-binary-installer")) {
//                        if (to.getProjectNode(projectName, projectType) == null) {
//                            to.addChildTo(rootNode, projectName, projectType, model);
//                        }
//                        to.getProjectNode(projectName, projectType).update_binary_installer = rz.getBytesFromFile(in);
//                        continue;
//                    } else if (filePath.contains("META-INF/com/google/android/updater-script")) {
//                        if (to.getProjectNode(projectName, projectType) == null) {
//                            to.addChildTo(rootNode, projectName, projectType, model);
//                        }
//                        to.getProjectNode(projectName, projectType).updater_script += rz.getStringFromFile(in);
//                        continue;
//                    } else if (filePath.contains("META-INF/com/google/android/aroma")) {
//                        continue;
//                    }
//                } else 
                if (projectType == ProjectNode.PROJECT_AROMA) {// || projectType == ProjectNode.PROJECT_NORMAL) {
                    if (filePath.startsWith("META-INF")) {
                        continue;
                    }
                }
                if (name.equals(Xml.custom_path)) {
                    Xml.originalData = rz.getStringFromFile(in);
                    containsCustomXml = true;
                    continue;
                } else if (name.equals(Xml.delete_path)) {
                    Xml.deleteData = rz.getStringFromFile(in);
                    containsDeleteXml = true;
                    continue;
                } else if (name.equals(Xml.data_path)) {
                    Xml.fileData = rz.getStringFromFile(in);
                    containsDataXml = true;
                    continue;
                }
                //file = to.addFileToTree(fileName, groupName, groupType, projectName, projectType);
                if (groupType == GroupNode.GROUP_OTHER) {
                    file.fileZipPath = filePath;
                } else if (groupType == GroupNode.GROUP_CUSTOM) {
                    //Xml.addFileDataToGroup(file);
                }
            }
            file.fileSourcePath = file.path;
            rz.writeFileFromZip(in, file.fileSourcePath);
        }

        Xml.terminate();

        if (containsCustomXml) {
            Xml.parseXml(GroupNode.GROUP_CUSTOM);//this sets values of custom group/subGroup
        }
        if (containsDeleteXml) {
            Xml.parseXml(GroupNode.GROUP_DELETE_FILES);//this creates new file objects of delete group
        }
        if (containsDataXml) {
            Xml.parseXml(0);
        }
        progressBarImportExport.setString("Successfully Imported");
        progressBarImportExport.setValue(100);
        JOptionPane.showMessageDialog(null, "Successfully Imported");
        progressBarImportExport.setString("0%");
        progressBarImportExport.setValue(0);
        progressBarFlag = 0;
    }

    public static void setCardLayout(int cardNo) {
        CardLayout cardLayout = (CardLayout) panelLower.getLayout();
        cardLayout.show(panelLower, "card" + Integer.toString(cardNo));
    }

    @Override
    public void run() {
        try {
            setCardLayout(2);
            fromTheZip(path);
            setCardLayout(1);
        } catch (IOException | ParserConfigurationException | TransformerException | SAXException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
