/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flashablezipcreator.Operations;

import flashablezipcreator.Core.FileNode;
import flashablezipcreator.Core.FolderNode;
import flashablezipcreator.Core.GroupNode;
import flashablezipcreator.Core.ProjectItemNode;
import flashablezipcreator.Core.SubGroupNode;
import flashablezipcreator.UserInterface.Preferences;
import flashablezipcreator.Protocols.Project;
import flashablezipcreator.DiskOperations.Read;
import flashablezipcreator.FlashableZipCreator;
import flashablezipcreator.Protocols.Types;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Rajat
 */
public class UpdateBinaryOperations {

    public static final int installString = 1;
    public static final int deleteString = 2;
    public static final int normalString = 3;
    public static final int copyString = 4;
    public static final String universalUpdateBinaryFilePath = "/flashablezipcreator/META-INF/com/google/android/universal-update-binary"; //Add universal binary to this path

    public String addPrintString(String str, int type) {
        switch (type) {
            case installString:
                return "ui_print \"@Installing " + str + "\"\n";
            case deleteString:
                return "ui_print \"@Deleting " + str + "\"\n";
            case copyString:
                return "ui_print \"@Copying " + str + "\"\n";
        }
        return "ui_print \"" + str + "\"\n";
    }

    public String addPrintString(String str) {
        return "ui_print \"" + str + "\"\n";
    }

    public String initiateUpdaterScript() throws FileNotFoundException, IOException {
        Read reader = new Read();
        String initShellScript = reader.getStringFromFile(FlashableZipCreator.class.getResourceAsStream(universalUpdateBinaryFilePath));
        return initShellScript + addPrintString("@Starting the install process")
                + addPrintString("Setting up required tools...");
    }

    public String terminateUpdaterScript() {
        return addPrintString("Unmounting Partitions...")
                + "unmount /data\n"
                + "unmount /system\n";
    }

    public String addWipeDalvikCacheString() {
        String str = "";
        str += "mount -o rw,remount,rw /data\n";
        str += "if [ $(file_getprop /tmp/aroma/dalvik_choices.prop true) ==  yes ]; then\n"
                + "ui_print \"@Wiping dalvik-cache\"\n"
                + "delete_recursive /data/dalvik-cache\n"
                + "fi;\n";
        str += "umount /data\n";
        return str;
    }

    public String getMountMethod(int type) {
        switch (type) {
            case 1:
                return addPrintString("@Mounting Partitions...")
                        + "mount /system\n"
                        + "mount /data\n"
                        + "if [ $(is_mounted /data) == 1 ]; then\n"
                        + addPrintString("/data already mounted.")
                        //                        + "umount /data\n"
                        //                        + "mount /data\n"
                        + "fi;\n"
                        + "mount -o rw,remount /system\n"
                        + "mount -o rw,remount /system /system\n"
                        + "mount -o rw,remount /\n"
                        + "mount -o rw,remount / /\n"
                        + addPrintString("Extracting zip contents to /tmp")
                        + "cd /tmp\n"
                        + "mkdir zipContent\n"
                        + "cd zipContent\n";
            case 2:
                //future aspect
                break;
        }
        return "";
    }

    public String getFolderScript(String str, ProjectItemNode parent) {
        str += createDirectory(((FolderNode) parent).prop.folderLocation.replaceAll("\\\\", "/"));
        if (((FolderNode) parent).prop.setPermissions) {
            str += "set_perm " + ((FolderNode) parent).prop.folderPermission + "\n";
        }
        for (ProjectItemNode child : parent.prop.children) {
            if (child.prop.type == Types.NODE_FOLDER) {
                str = getFolderScript(str, child);
            } else if (child.prop.type == Types.NODE_FILE) {
                FileNode file = (FileNode) child;
                if (file.prop.title.endsWith("apk")) {
                    str += addPrintString("Copying " + file.prop.title);
                    FolderNode folder = (FolderNode) (file.prop.parent);
                    GroupNode group = (GroupNode) (folder.prop.originalParent);
                    if (group.prop.groupType == Types.GROUP_DATA_APP) {
                        str += "package_extract_file \"" + file.prop.fileZipPath + "\" \"" + file.prop.fileInstallLocation + "/" + "base.apk" + "\"\n";
                    } else {
                        str += "package_extract_file \"" + file.prop.fileZipPath + "\" \"" + file.prop.fileInstallLocation + "/" + file.prop.title + "\"\n";
                    }
                } else {
                    str += addPrintString("Copying " + file.prop.title);
                    str += "package_extract_file \"" + file.prop.fileZipPath + "\" \"" + file.prop.fileInstallLocation + "/" + file.prop.title + "\"\n";
                }
                if (file.prop.setPermissions) {
                    str += "set_perm " + file.prop.filePermission + "\n";  //TODO: Inspect filePermission for removal of commas
                }
            }
        }
        return str;
    }

    public String predefinedFolderGroupScript(GroupNode node) {
        String str = "";
        if (node.isCheckBox()) {
            int count = 1;
            if (Preferences.IsFromLollipop) {
                str += "if [ $(file_getprop /tmp/aroma/" + node.prop.propFile + " item.1." + count++ + ") == 1 ]; then\n";
                for (ProjectItemNode folder : node.prop.children) {
                    str += addPrintString(folder.prop.title, installString);
                    str = getFolderScript(str, folder);
                }
                str += "fi;\n";
                for (ProjectItemNode folder : node.prop.children) {
                    str += "if [ $(file_getprop /tmp/aroma/" + node.prop.propFile + " item.1." + count++ + ") == 1 ]; then\n";
                    str += addPrintString(folder.prop.title, installString);
                    str = getFolderScript(str, folder);
                    str += "fi;\n";
                }
            } else {
                str = predefinedGroupScript(node);
            }
        }
        return str;
    }

    public String predefinedGroupScript(GroupNode node) {
        String str = "";
        if (node.isCheckBox()) {
            int count = 1;
//            str += getPackageExtractDirString(node);
            str += "if [ $(file_getprop /tmp/aroma/" + node.prop.propFile + " item.1." + count++ + ") == 1 ]; then\n";
            for (ProjectItemNode fnode : node.prop.children) {
                FileNode file = (FileNode) fnode;
                if (node.prop.groupType == Types.GROUP_DELETE_FILES) {
                    str += addPrintString(((FileNode) file).prop.title, deleteString);
                    str += "delete /" + ((FileNode) file).getDeleteLocation() + "\n";
                } else {
                    str += addPrintString(file.prop.title, copyString);
                    str += "package_extract_file \"" + ((FileNode) file).prop.fileZipPath + "\" \"" + ((FileNode) file).prop.fileInstallLocation + "/" + ((FileNode) file).prop.title + "\"\n";
                    if (((FileNode) file).prop.setPermissions) {
                        str += "set_perm " + ((FileNode) file).prop.filePermission + "\n";
                    }
                }
            }
            str += "fi;\n";
            for (ProjectItemNode file : node.prop.children) {
                str += "if [ $(file_getprop /tmp/aroma/" + node.prop.propFile + " item.1." + count++ + ") == 1 ]; then\n";
                if (node.prop.groupType == Types.GROUP_DELETE_FILES) {
                    str += addPrintString(((FileNode) file).prop.title, deleteString);
                    str += "delete /" + ((FileNode) file).getDeleteLocation() + "\n";
                } else {
                    str += addPrintString(((FileNode) file).prop.title, copyString);
                    str += "package_extract_file \"" + ((FileNode) file).prop.fileZipPath + "\" \"" + ((FileNode) file).prop.fileInstallLocation + "/" + ((FileNode) file).prop.title + "\"\n";
                    if (((FileNode) file).prop.setPermissions) {
                        str += "set_perm " + ((FileNode) file).prop.filePermission + "\n";
                    }
                }
                str += "fi;\n";
            }
        } else if (node.isSelectBox() && node.prop.groupType == Types.GROUP_SCRIPT) {
            int count = 2;
            for (ProjectItemNode file : node.prop.children) {
                str += "if [ $(file_getprop /tmp/aroma/" + node.prop.propFile + " selected.1 ) == " + count++ + " ]; then\n";
                str += addPrintString("@Running script : " + ((FileNode) file).prop.title);
                str += "package_extract_file \"" + ((FileNode) file).prop.fileZipPath + "\" /tmp/script\n"
                        + "set_perm 0 0 0777 /tmp/script\n"
                        + "./tmp/script\n" //TODO: CHECK Here
                        + "delete /tmp/script\n";
                str += "fi;\n";
            }
        } else {
            System.out.println("This Group is not supported");
        }
        return str;
    }

    public String deleteTempFiles() {
        String str = "";
        for (String path : Project.getTempFilesList()) {
            str += "delete /" + path + "\n";
        }
        return str;
    }

    public String predefinedSubGroupsScript(GroupNode node) {
        String str = "";
        if (node.isSelectBox()) {
            int count = 2;
//            str += getPackageExtractDirString(node);
            for (ProjectItemNode subGroup : node.prop.children) {
                if (node.prop.groupType == Types.GROUP_SYSTEM_FONTS) {
                    str += "if [ $(file_getprop /tmp/aroma/" + node.prop.propFile.replace(".prop", "_temp.prop") + " " + subGroup.toString() + ") == \"" + "yes" + "\" ]; then\n";
                } else {
                    str += "if [ $(file_getprop /tmp/aroma/" + node.prop.propFile + " selected.1 ) == " + count++ + " ]; then\n";
                }
                str += addPrintString(((SubGroupNode) subGroup).prop.title, installString);
                for (ProjectItemNode file : subGroup.prop.children) {
                    switch (node.prop.groupType) {
                        case Types.GROUP_SYSTEM_FONTS:
                            str += "package_extract_file \"" + ((FileNode) file).prop.fileZipPath + "\" \"" + ((FileNode) file).prop.fileInstallLocation + "/" + ((FileNode) file).prop.title + "\"\n";
                            if (((FileNode) file).prop.setPermissions) {
                                str += "set_perm " + ((FileNode) file).prop.filePermission + "\n";
                            }
                            break;
                        case Types.GROUP_DATA_LOCAL:
                        case Types.GROUP_SYSTEM_MEDIA:
                            //this will rename any zip package to bootamination.zip allowing users to add bootanimation.zip with custom names.
                            str += "package_extract_file \"" + ((FileNode) file).prop.fileZipPath + "\" \"" + ((FileNode) file).prop.fileInstallLocation + "/" + "bootanimation.zip" + "\"\n";
                            if (((FileNode) file).prop.setPermissions) {
                                str += "set_perm " + ((FileNode) file).prop.filePermission + "\n";
                            }
                            break;
                    }
                }
                str += "fi;\n";
            }
        }
        return str;
    }

    public String customGroupScript(GroupNode node) {
        String str = "";
        if (node.isCheckBox()) {
            int count = 1;
            str += "if [ $(file_getprop /tmp/aroma/" + node.prop.propFile + " item.1." + count++ + ") == 1 ]; then\n";
            for (ProjectItemNode tempNode : node.prop.children) {
                switch (tempNode.prop.type) {
                    case Types.NODE_FOLDER:
                        str += addPrintString(((FolderNode) tempNode).prop.title, installString);
                        str = getFolderScript(str, (FolderNode) tempNode);
                        break;
                    case Types.NODE_FILE:
                        str += addPrintString(((FileNode) tempNode).prop.title, installString);
                        str += "package_extract_file \"" + ((FileNode) tempNode).prop.fileZipPath + "\" \"" + ((FileNode) tempNode).prop.fileInstallLocation + "/" + ((FileNode) tempNode).prop.title + "\"\n";
                        if (((FileNode) tempNode).prop.setPermissions) {
                            str += "set_perm " + ((FileNode) tempNode).prop.filePermission + "\n";
                        }
                }
            }
            str += "fi;\n";
            for (ProjectItemNode tempNode : node.prop.children) {
                switch (tempNode.prop.type) {
                    case Types.NODE_FOLDER:
                        str += "if [ $(file_getprop /tmp/aroma/" + node.prop.propFile + " item.1." + count++ + ") == 1 ]; then\n";
                        str += addPrintString(((FolderNode) tempNode).prop.title, installString);
                        str = getFolderScript(str, (FolderNode) tempNode);
                        str += "fi;\n";
                        break;
                    case Types.NODE_FILE:
                        str += "if [ $(file_getprop /tmp/aroma/" + node.prop.propFile + " item.1." + count++ + ") == 1 ]; then\n";
                        str += addPrintString(((FileNode) tempNode).prop.title, installString);
                        str += "package_extract_file \"" + ((FileNode) tempNode).prop.fileZipPath + "\" \"" + ((FileNode) tempNode).prop.fileInstallLocation + "/" + ((FileNode) tempNode).prop.title + "\"\n";
                        if (((FileNode) tempNode).prop.setPermissions) {
                            str += "set_perm " + ((FileNode) tempNode).prop.filePermission + "\n";
                        }
                        str += "fi;\n";
                }
            }
        } else if (node.isSelectBox()) {
            int count = 2;
            for (ProjectItemNode tempNode : node.prop.children) {
                switch (tempNode.prop.type) {
                    case Types.NODE_FOLDER:
                        str += "if [ $(file_getprop /tmp/aroma/" + node.prop.propFile + " selected.1 ) == " + count++ + " ]; then\n";
                        str += addPrintString(((FolderNode) tempNode).prop.title, installString);
                        str = getFolderScript(str, (FolderNode) tempNode);
                        str += "fi;\n";
                        break;
                    case Types.NODE_FILE:
                        str += "if [ $(file_getprop /tmp/aroma/" + node.prop.propFile + " selected.1 ) ==  " + count++ + " ]; then\n";
                        str += addPrintString(((FileNode) tempNode).prop.title, installString);
                        str += "package_extract_file \"" + ((FileNode) tempNode).prop.fileZipPath + "\" \"" + ((FileNode) tempNode).prop.fileInstallLocation + "/" + ((FileNode) tempNode).prop.title + "\"\n";
                        if (((FileNode) tempNode).prop.setPermissions) {
                            str += "set_perm " + ((FileNode) tempNode).prop.filePermission + "\n";
                        }
                        str += "fi;\n";
                }
            }
            str += "fi;\n";
        }
        return str;
    }

    public String generateUpdaterScript(GroupNode node) {
        switch (node.prop.groupType) {
            //Group of predefined locations
            case Types.GROUP_SYSTEM_APK:
            case Types.GROUP_SYSTEM_PRIV_APK:
            case Types.GROUP_DATA_APP:
                return predefinedFolderGroupScript(node);
            case Types.GROUP_SYSTEM_MEDIA_AUDIO_ALARMS:
            case Types.GROUP_SYSTEM_MEDIA_AUDIO_NOTIFICATIONS:
            case Types.GROUP_SYSTEM_MEDIA_AUDIO_RINGTONES:
            case Types.GROUP_SYSTEM_MEDIA_AUDIO_UI:
            case Types.GROUP_DELETE_FILES:
            case Types.GROUP_SCRIPT:
                return predefinedGroupScript(node);
            //Group of predefined locations that need subgroups
            case Types.GROUP_SYSTEM_FONTS:
            case Types.GROUP_DATA_LOCAL:
            case Types.GROUP_SYSTEM_MEDIA:
                return predefinedSubGroupsScript(node);
            //Group of custom location.
            case Types.GROUP_CUSTOM:
                return customGroupScript(node);
        }
        return "";
    }

    public String setProgress(int progress) {
        return "\nset_progress " + progress + "\n";
    }

    public String getSymlinkScript() {
        return "#!/system/bin/mksh\n"
                + "\n"
                + "mount -o remount rw /system\n"
                + "cd /preload/symlink/system/app\n"
                + "\n"
                + "# Can't create array with /sbin/sh, hence we use mksh\n"
                + "apk_list=( `ls | grep .apk` )\n"
                + "odex_list=( `ls | grep .odex` )\n"
                + "items=${apk_list[*]}\" \"${odex_list[*]}\n"
                + "\n"
                + "for item in ${items[@]}\n"
                + "do\n"
                + "  ln -s /preload/symlink/system/app/$item /system/app/$item \n"
                + "done";
    }

    public String getDpiScript(String dpi) {
        return "#!/sbin/sh\n"
                + "sed -i '/ro.sf.lcd_density=/s/240/" + dpi + "/g' /system/build.prop";
    }

    public String getExtractDataString() {
        return "package_extract_dir data\\ /data\n"
                + "set_perm 2000 2000 0771 /data/local\n"
                + "set_perm_recursive 1000 1000 0771 0644 /data/app\n";
    }

    //following is not needed as of now as creating directory is handled in newer update-binary-installer
    public String createDirectory(String dir) {
        return "mkdir -p \"" + dir + "\"\n";
    }
}
