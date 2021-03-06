/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flashablezipcreator.Core;

import flashablezipcreator.Protocols.Logs;
import flashablezipcreator.Protocols.Project;
import flashablezipcreator.Protocols.Types;
import flashablezipcreator.UserInterface.Preference;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Nikhil
 */
public final class NodeProperties {

    public String title;
    public String path;
    public String zipPath;
    public String location = "";
    public String groupName;
    public String subGroupName;
    public String permission = "";
    public String owner = "";
    public String group = "";
    public String perm = "";
//    public String groupPermission = "";
    public String folderPermission = "";
    public String defaultFolderPerm = "";
    public String propFile;
    public String propType;
    public String extension = "";
    public String projectName;
    public String originalGroupType;
    public String updater_script = "";
    public String aroma_config = "";
    public String androidVersion = Project.androidVersion;
    public String releaseVersion = Project.releaseVersion;
    public String projectZipPathPrefix = "Project_";
    public String groupZipPathPrefix = "Group_";
    public String subGroupZipPathPrefix = "SubGroup_";
    public String folderZipPathPrefix = "Folder_";
    public String typePrefix = "Type_";
    public String locSeparator = "Loc_";
    public String permSeparator = "Perm_";
    public String folderName;
    public String folderLocation;
    public String description = "";
    public String fileName;
    public String fileSourcePath;
    public String fileInstallLocation;
    public String filePermission;
    public String fileZipPath;
    public String value;
    public String deletePath;
    public String folderMenuName;
    public int type;
    public int projectType;
    public int groupType;
    public int subGroupType;
    public int modType;
    public int packageType;
    public boolean createZip = true; //when multiple projects will be loaded, this will help in choosing which one to create zip of.
    public boolean isBootAnimationGroup = false;
    public boolean isSelectBox = false;
    public boolean setPermissions = true;
    public ProjectItemNode parent;
    public ProjectItemNode originalParent;
    public GroupNode groupParent;
    public SubGroupNode subGroupParent;
    public ProjectNode projectParent;
    public FolderNode folderParent;
    public ArrayList<ProjectItemNode> children = new ArrayList<>();
    public byte[] update_binary = null;
    public byte[] update_binary_installer = null;

    public NodeProperties() {

    }

    //creates properties of folder having Group parent
    public NodeProperties(String title, GroupNode parent) {
        this.title = title;
        this.type = Types.NODE_FOLDER;
        this.parent = parent;
        originalParent = parent;
        groupParent = parent;
        if (parent.prop.groupType == Types.GROUP_DATA_APP) {
            if (!(title.contains("-"))) {
                this.title += "-1";
                title = this.title;
            }
        }
        folderName = title;
        path = parent.prop.path + File.separator + title;
        zipPath = parent.prop.zipPath + "/" + folderZipPathPrefix + title;
        location = parent.prop.location;
        folderLocation = parent.prop.location + File.separator + title;
        permission = parent.prop.permission;
        owner = parent.prop.owner;
        group = parent.prop.group;
        perm = parent.prop.perm;
        setPermissions = parent.prop.setPermissions;
        defaultFolderPerm = (Preference.pp.useUniversalBinary) ? "1000" + " " + "1000" + " " + "0755" + " "
                : "1000" + ", " + "1000" + ", " + "0755" + ", ";
        setPermissions();
        projectName = parent.prop.projectName;
        groupName = parent.prop.groupName;
        groupType = parent.prop.groupType;
        originalGroupType = parent.prop.originalGroupType;
        packageType = parent.prop.packageType;
    }

    //creates properties of Folder having Folder parent
    public NodeProperties(String title, FolderNode parent) {
        this.title = title;
        this.type = Types.NODE_FOLDER;
        this.parent = parent;
        originalParent = parent.prop.originalParent;
        groupParent = parent.prop.groupParent;
        folderName = title;
        path = parent.prop.path + File.separator + title;
        zipPath = parent.prop.zipPath + "/" + folderZipPathPrefix + title;
        location = parent.prop.location;
        folderLocation = parent.prop.folderLocation + File.separator + title;
        permission = parent.prop.permission;
        owner = parent.prop.owner;
        group = parent.prop.group;
        perm = parent.prop.perm;
        setPermissions = parent.prop.setPermissions;
        defaultFolderPerm = (Preference.pp.useUniversalBinary) ? "1000" + " " + "1000" + " " + "0755" + " "
                : "1000" + ", " + "1000" + ", " + "0755" + ", ";
        setPermissions();
        projectName = parent.prop.projectName;
        originalGroupType = parent.prop.originalGroupType;
        groupName = parent.prop.originalParent.getTitle();
        groupType = parent.prop.groupParent.prop.groupType;
        packageType = parent.prop.packageType;
    }

    //creates properties of Project
    public NodeProperties(String title, int type, int modType, ProjectItemNode parent) {
        this.title = title;
        this.type = Types.NODE_PROJECT;
        this.parent = parent;
        this.modType = modType;
        projectName = title;
        projectType = type;
        path = parent + File.separator + title;
        switch (projectType) {
            case Types.PROJECT_AROMA:
                zipPath = parent.prop.zipPath + "/" + "aroma_" + modType + "/" + projectZipPathPrefix + title;
                break;
            case Types.PROJECT_CUSTOM:
                zipPath = parent.prop.zipPath + "/" + "custom_" + modType + "/" + projectZipPathPrefix + title;
                break;
            case Types.PROJECT_MOD:
                zipPath = parent.prop.zipPath + "/" + "mod_" + modType + "/" + projectZipPathPrefix + title;
                break;
            case Types.PROJECT_GAPPS:
                zipPath = parent.prop.zipPath + "/" + "gapps_" + modType + "/" + projectZipPathPrefix + title;
                break;
        }

        androidVersion = "5.x+";
    }

    //creates properties of Group
    public NodeProperties(String title, int type, ProjectNode parent) {
        this.title = title;
        this.type = Types.NODE_GROUP;
        this.parent = parent;
        groupType = type;
        groupName = title;
        path = parent.prop.path + File.separator + title;
        projectName = parent.prop.projectName;
        projectParent = parent;
        switch (type) {
            case Types.GROUP_SYSTEM:
                propType = "system";
                propFile = getProp();
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/system";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system";
                packageType = Types.PACKAGE_FOLDER_FILE;
                folderMenuName = "System Files";
                break;
            case Types.GROUP_VENDOR:
                propType = "vendor";
                propFile = getProp();
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/vendor";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "vendor";
                packageType = Types.PACKAGE_FOLDER_FILE;
                folderMenuName = "Vendor Files";
                break;
            case Types.GROUP_SYSTEM_APK:
                propType = "system_app";
                propFile = getProp();
                extension = "apk";
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/system/app";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system_app";
                packageType = Types.PACKAGE_APP;
                folderMenuName = "System Apps";
                break;
            case Types.GROUP_VENDOR_APP:
                propType = "vendor_app";
                propFile = getProp();
                extension = "apk";
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/vendor/app";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "vendor_app";
                packageType = Types.PACKAGE_APP;
                folderMenuName = "Vendor Apps";
                break;
            case Types.GROUP_SYSTEM_PRIV_APK:
                propType = "system_priv";
                propFile = getProp();
                extension = "apk";
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/system/priv-app";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system_priv_app";
                packageType = Types.PACKAGE_APP;
                folderMenuName = "Priv Apps";
                break;
            case Types.GROUP_SYSTEM_BIN:
                propType = "system_bin";
                propFile = getProp();
                owner = "0";
                group = "2000";
                perm = "0755";
                location = "/system/bin";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system_bin";
                packageType = Types.PACKAGE_FOLDER_FILE;
                folderMenuName = "System Bin";
                break;
            case Types.GROUP_SYSTEM_XBIN:
                propType = "system_xbin";
                propFile = getProp();
                owner = "0";
                group = "2000";
                perm = "0755";
                location = "/system/xbin";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system_xbin";
                packageType = Types.PACKAGE_FOLDER_FILE;
                folderMenuName = "System xBin";
                break;
            case Types.GROUP_VENDOR_BIN:
                propType = "vendor_bin";
                propFile = getProp();
                owner = "0";
                group = "2000";
                perm = "0755";
                location = "/vendor/bin";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "vendor_bin";
                packageType = Types.PACKAGE_FOLDER_FILE;
                folderMenuName = "Vendor Bin";
                break;
            case Types.GROUP_SYSTEM_ETC:
                propType = "system_etc";
                propFile = getProp();
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/system/etc";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system_etc";
                packageType = Types.PACKAGE_FOLDER_FILE;
                folderMenuName = "System Etc";
                break;
            case Types.GROUP_VENDOR_ETC:
                propType = "vendor_etc";
                propFile = getProp();
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/vendor/etc";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "vendor_etc";
                packageType = Types.PACKAGE_FOLDER_FILE;
                folderMenuName = "Vendor Etc";
                break;
            case Types.GROUP_SYSTEM_FRAMEWORK:
                propType = "system_framework";
                propFile = getProp();
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/system/framework";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system_framework";
                packageType = Types.PACKAGE_FOLDER_FILE;
                folderMenuName = "System Framework";
                break;
            case Types.GROUP_VENDOR_FRAMEWORK:
                propType = "vendor_framework";
                propFile = getProp();
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/vendor/framework";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "vendor_framework";
                packageType = Types.PACKAGE_FOLDER_FILE;
                folderMenuName = "Vendor Framework";
                break;
            case Types.GROUP_SYSTEM_LIB:
                propType = "system_lib";
                propFile = getProp();
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/system/lib";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system_lib";
                packageType = Types.PACKAGE_FOLDER_FILE;
                folderMenuName = "System Lib";
                break;
            case Types.GROUP_VENDOR_LIB:
                propType = "vendor_lib";
                propFile = getProp();
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/vendor/lib";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "vendor_lib";
                packageType = Types.PACKAGE_FOLDER_FILE;
                folderMenuName = "Vendor Lib";
                break;
            case Types.GROUP_SYSTEM_LIB64:
                propType = "system_lib64";
                propFile = getProp();
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/system/lib64";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system_lib64";
                packageType = Types.PACKAGE_FOLDER_FILE;
                folderMenuName = "System Lib64";
                break;
            case Types.GROUP_VENDOR_LIB64:
                propType = "vendor_lib64";
                propFile = getProp();
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/vendor/lib64";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "vendor_lib64";
                packageType = Types.PACKAGE_FOLDER_FILE;
                folderMenuName = "Vendor Lib64";
                break;
            case Types.GROUP_SYSTEM_MEDIA_AUDIO_ALARMS:
                propType = "system_media_alarms";
                propFile = getProp();
                extension = "audio";
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/system/media/audio/alarms";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system_media_alarms";
                packageType = Types.PACKAGE_FILE;
                folderMenuName = "Alarm Tones";
                break;
            case Types.GROUP_SYSTEM_MEDIA_AUDIO_NOTIFICATIONS:
                propType = "system_media_notifications";
                propFile = getProp();
                extension = "audio";
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/system/media/audio/notifications";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system_media_notifications";
                packageType = Types.PACKAGE_FILE;
                folderMenuName = "Notifications";
                break;
            case Types.GROUP_SYSTEM_MEDIA_AUDIO_RINGTONES:
                propType = "system_media_ringtones";
                propFile = getProp();
                extension = "audio";
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/system/media/audio/ringtones";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system_media_ringtones";
                packageType = Types.PACKAGE_FILE;
                folderMenuName = "Ringtones";
                break;
            case Types.GROUP_SYSTEM_MEDIA_AUDIO_UI:
                propType = "system_media_ui";
                propFile = getProp();
                extension = "audio";
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/system/media/audio/ui";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system_media_ui";
                packageType = Types.PACKAGE_FILE;
                folderMenuName = "UI Tones";
                break;
            case Types.GROUP_SYSTEM_MEDIA:
                propType = "system_media";
                propFile = getProp();
                isSelectBox = true;
                extension = "zip";
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/system/media";
                setPermissions(owner, group, perm, "bootanimation.zip");
                isBootAnimationGroup = true;
                originalGroupType = typePrefix + "system_media";
                packageType = Types.PACKAGE_SUBGROUP_FILE;
                folderMenuName = "Boot Animations";
                break;
            case Types.GROUP_SYSTEM_FONTS:
                propType = "system_fonts";
                propFile = getProp();
                isSelectBox = true;
                extension = "ttf";
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/system/fonts";
                setPermissions(owner, group, perm);
                originalGroupType = typePrefix + "system_fonts";
                packageType = Types.PACKAGE_SUBGROUP_FILE;
                folderMenuName = "Fonts";
                break;
            case Types.GROUP_DATA_APP:
                propType = "data_app";
                propFile = getProp();
                extension = "apk";
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/data/app";
                setPermissions(owner, group, perm, "base/apk");
                originalGroupType = typePrefix + "data_app";
                packageType = Types.PACKAGE_APP;
                folderMenuName = "Data Apps";
                break;
            case Types.GROUP_DATA_LOCAL:
                propType = "data_local";
                propFile = getProp();
                isSelectBox = true;
                extension = "zip";
                owner = "0";
                group = "0";
                perm = "0644";
                location = "/data/local";
                setPermissions(owner, group, perm, "bootanimation.zip");
                isBootAnimationGroup = true;
                originalGroupType = typePrefix + "data_local";
                packageType = Types.PACKAGE_SUBGROUP_FILE;
                folderMenuName = "Boot Animations";
                break;
            case Types.GROUP_CUSTOM:
                propType = "custom";
                propFile = getProp();
                isSelectBox = false;
                packageType = Types.PACKAGE_CUSTOM;
                folderMenuName = "Custom Files";
                break;
            case Types.GROUP_MOD:
                propType = "mod";
                //following properties not needed but added.
                extension = "zip";
                propFile = getProp();
                isSelectBox = false;
                originalGroupType = typePrefix + "mod";
                folderMenuName = "Mods";
                packageType = Types.PACKAGE_MOD_FILE;
                isSelectBox = (title.toLowerCase().contains("kernel"));
                break;
            case Types.GROUP_AROMA_THEMES:
                Logs.write("adding themes");
                propFile = "themes.prop";
                isSelectBox = true;
                path = "META-INF/com/google/android/aroma/themes" + File.separator + title;
                extension = "themes";
                packageType = Types.PACKAGE_THEME;
                folderMenuName = "Themes";
                break;
            case Types.GROUP_DELETE_FILES:
                propType = "delete";
                propFile = getProp();
                isSelectBox = false;
                extension = "delete";
                originalGroupType = typePrefix + "delete";
                packageType = Types.PACKAGE_DELETE_FILE;
                folderMenuName = "Files/Folders to Delete";
                break;
            case Types.GROUP_SCRIPT:
                propType = "dpi";
                propFile = getProp();
                isSelectBox = true;
                extension = "sh";
                originalGroupType = typePrefix + "script";
                break;
        }
        zipPath = parent.prop.zipPath + "/" + originalGroupType + "/" + groupZipPathPrefix + title;
        Logs.write("group property ready");
    }

    //creates properties of SubGroup
    public NodeProperties(String title, int type, GroupNode parent) {
        this.title = title;
        this.type = Types.NODE_SUBGROUP;
        this.parent = parent;
        originalParent = parent;
        path = parent.prop.path + File.separator + title;
        subGroupName = title;
        groupType = type;
        subGroupType = type;
        projectName = parent.prop.projectName;
        originalGroupType = parent.prop.originalGroupType;
        location = parent.prop.location;
        packageType = parent.prop.packageType;
        switch (type) {
            case Types.GROUP_SYSTEM_FONTS:
                extension = "ttf";
                break;
            case Types.GROUP_SYSTEM_MEDIA:
                extension = "zip";
                isBootAnimationGroup = true;
                break;
            case Types.GROUP_DATA_LOCAL:
                extension = "zip";
                isBootAnimationGroup = true;
                break;
        }
        zipPath = parent.prop.zipPath + "/" + subGroupZipPathPrefix + title;
        permission = parent.prop.permission;
        setPermissions = parent.prop.setPermissions;
    }

    public String getProp() {
        return propType + "_" + groupName.replaceAll(" ", "_") + "_" + parent.prop.title.replaceAll(" ", "_") + ".prop";
    }

    public void reloadOriginalStringType() {
        originalGroupType = typePrefix + "custom_" + locSeparator + location.replaceAll("/", "+") + "_" + permSeparator + owner + "-" + group + "-" + perm;
    }

    public void reloadZipPath() {
        zipPath = parent.prop.zipPath + "/" + originalGroupType + "/" + groupZipPathPrefix + title;
    }

    public void reloadZipPath(String newTitle) {
        zipPath = parent.prop.zipPath + "/" + originalGroupType + "/" + groupZipPathPrefix + newTitle;
    }

    public void updateFileZipPath() {
        fileZipPath = parent.prop.zipPath + "/" + title;
    }

    public void updateFileInstallLocation() {
        fileInstallLocation = parent.prop.location.replaceAll("\\\\", "/");
    }

    public void setPermissions() {
        folderPermission = defaultFolderPerm + "\"" + folderLocation + "\"";
        folderPermission = folderPermission.replaceAll("\\\\", "/");
        if (Preference.pp.useUniversalBinary) {
            permission = owner + " " + group + " " + perm + " ";
        } else {
            permission = owner + ", " + group + ", " + perm + ", ";
        }
    }

    public void setPermissions(String parentPermission, String title) {
        filePermission = (parentPermission + "\"" + fileInstallLocation + "/" + title + "\"").replaceAll("\\\\", "/");
    }

    public void setPermissions(String o, String g, String p) {
        owner = o;
        group = g;
        perm = p;
        if (Preference.pp.useUniversalBinary) {
            permission = owner + " " + group + " " + perm + " ";
        } else {
            permission = owner + ", " + group + ", " + perm + ", ";
        }
    }

    public void setPermissions(String o, String g, String p, String title) {
        owner = o;
        group = g;
        perm = p;
        if (Preference.pp.useUniversalBinary) {
            permission = owner + " " + group + " " + perm + " ";
        } else {
            permission = owner + ", " + group + ", " + perm + ", ";
        }
        filePermission = (permission + "\"" + fileInstallLocation + "/" + title + "\"").replaceAll("\\\\", "/");
    }
}
