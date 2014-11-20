/*
NOTES:
Missing object on destination ORG --> ERROR Updating Record: Sobject Type Name: bad value for restricted picklist field: Object_Name__c
Missing Field on destination ORG --> ERROR Updating Record: Field Name: bad value for restricted picklist field: Account.Field_Name__c
*/

package sfdcPermissionSet;

//import java.io.BufferedWriter;
//import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
//import java.util.*;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
//import java.io.FileWriter;
//import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Set;

import sfdcPermissionSet.SfdcCommon;

import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.*;
import com.sforce.soap.partner.*;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.ConnectionException;
//import com.sforce.soap.partner.Error;
//import com.sforce.ws.ConnectionException;
//import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.util.Base64;

@SuppressWarnings("unused")
public class CopyPermissionSet {

    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    static final String authEndPointDefault = "https://test.salesforce.com/services/Soap/u/30.0/";
    static final String authEndPointSandBox = "https://test.salesforce.com/services/Soap/u/30.0/";
    static final String authEndPointProd = "https://login.salesforce.com/services/Soap/u/30.0/";
    static final String USERNAME = "";
    static final String PASSWORD = "";
    //static PartnerConnection connection;
    static PartnerConnection connectionSource;
    static PartnerConnection connectionTarget;
    static ConnectorConfig configSource;
    static ConnectorConfig configTarget;

    static List<String> stracct = new ArrayList<String>();
    static Map<String, String> idMap = new HashMap<String, String>();
    static Map<String, String> csvMap = new HashMap<String, String>();

  //------------------------------------------------------------------------------------------------
    /**
     * @Desc: Main method to call this utility
     * @Param:
     * @Return:
     */

    /**
     * TODO: Set the parameters:
     * sourcePermissionSetId
     * targetPermissionSetId
     * Source Org "userName", "Password", authEndPointSandBox
     * Target Org "userName", "Password", authEndPointSandBox
     */
    
    public static void main(String[] args) throws ConnectionException {
        System.out.println("Start Date/Time : " + (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date()));
        
        String sourcePermissionSetId = "sourcePermissionSetId";
        String targetPermissionSetId = "targetPermissionSetId";
        
        connectionSource = SfdcCommon.doLogin("userName", "Password", authEndPointSandBox);
        connectionTarget = SfdcCommon.doLogin("UserName", "Password", authEndPointSandBox);
        
        if ( connectionSource != null && connectionTarget != null) {

            processCopyPermissionSet(sourcePermissionSetId, targetPermissionSetId);

            SfdcCommon.doLogout(connectionSource);
            SfdcCommon.doLogout(connectionTarget);
        }

        System.out.println("End Date/Time: " + (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date()));

    }

//------------------------------------------------------------------------------------------------
    /**
     * @Desc:
     * @Param:
     * @Return:
     */
    public static void processCopyPermissionSet(String sourcePermissionSetId, String targetPermissionSetId) {
        try {
            System.out.println("\n---processCopyPermissionSet Starts---\n");
            
            CopyPermissionSet theCaller = new CopyPermissionSet();
            theCaller.createUpdateObjectPermissions(sourcePermissionSetId, targetPermissionSetId);
            theCaller.createUpdateFieldPermissions(sourcePermissionSetId, targetPermissionSetId);
            theCaller.processCopySetupEntityAccess(sourcePermissionSetId, targetPermissionSetId);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

//------------------------------------------------------------------------------------------------
    /**
     * @Desc:
     * @Param:
     * @Return:
     */
    private void createUpdateObjectPermissions(String sourcePermissionSetId, String targetPermissionSetId) {
    	System.out.println("\n---createUpdateObjectPermissions Starts---\n");
    	
    	try {
            String soqlObjectPermissions = "SELECT Id,ParentId,PermissionsCreate,PermissionsDelete,PermissionsEdit,PermissionsModifyAllRecords,PermissionsRead,PermissionsViewAllRecords,SobjectType FROM ObjectPermissions";

            Map<String, SObject> mapSourceObjectPermissions = SfdcCommon.querySfdcObject(connectionSource, soqlObjectPermissions + " WHERE ParentId = \'" + sourcePermissionSetId + "\'");
            Map<String, SObject> mapTargetObjectPermissions = SfdcCommon.querySfdcObject(connectionTarget, soqlObjectPermissions + " WHERE ParentId = \'" + targetPermissionSetId + "\'");

            Map<String, String> mapTargetObjectNameToId = new HashMap<String, String>();
            for (SObject record : mapTargetObjectPermissions.values()) {
                mapTargetObjectNameToId.put((String) record.getField("SobjectType"), (String) record.getField("Id"));
            }

            ArrayList<SObject> listRecordsToCreateUpdate = new ArrayList<SObject>();
            for (SObject sourceRecord : mapSourceObjectPermissions.values()) {
                SObject sObjectnew = new SObject();
                sObjectnew.setType("ObjectPermissions");
                String objectName = (String) sourceRecord.getField("SobjectType");
                if (mapTargetObjectNameToId.containsKey(objectName)) {
                    sObjectnew.setField("Id", mapTargetObjectNameToId.get(objectName));
                }

                sObjectnew.setField("ParentId", targetPermissionSetId);
                sObjectnew.setField("SobjectType", (String) sourceRecord.getField("SobjectType"));
                sObjectnew.setField("PermissionsRead", "true".equalsIgnoreCase((String) sourceRecord.getField("PermissionsRead")) ? true : false);
                sObjectnew.setField("PermissionsViewAllRecords", "true".equalsIgnoreCase((String) sourceRecord.getField("PermissionsViewAllRecords")) ? true : false);
                sObjectnew.setField("PermissionsCreate", "true".equalsIgnoreCase((String) sourceRecord.getField("PermissionsCreate")) ? true : false);
                sObjectnew.setField("PermissionsDelete", "true".equalsIgnoreCase((String) sourceRecord.getField("PermissionsDelete")) ? true : false);
                sObjectnew.setField("PermissionsEdit", "true".equalsIgnoreCase((String) sourceRecord.getField("PermissionsEdit")) ? true : false);
                sObjectnew.setField("PermissionsModifyAllRecords", "true".equalsIgnoreCase((String) sourceRecord.getField("PermissionsModifyAllRecords")) ? true : false);
                listRecordsToCreateUpdate.add(sObjectnew);
            }

            // Create/Update the records
            if (listRecordsToCreateUpdate != null && listRecordsToCreateUpdate.size() > 0) {
                System.out.println("Number of Records: " + listRecordsToCreateUpdate.size());
                SfdcCommon.createUpdateSfdcRecords(connectionTarget, listRecordsToCreateUpdate, 50);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    	
    	System.out.println("\n----createUpdateObjectPermissions Ends ---\n");
    }

//------------------------------------------------------------------------------------------------
    /**
     * @Desc:
     * @Param:
     * @Return:
     */
    private void createUpdateFieldPermissions(String sourcePermissionSetId, String targetPermissionSetId) {
        
    	System.out.println("\n----createUpdateFieldPermissions Starts ---\n");
    	
    	try {
            String soqlFieldPermissions = "SELECT Field, Id, ParentId, PermissionsEdit, PermissionsRead, SobjectType FROM FieldPermissions";

            Map<String, SObject> mapSourceFieldPermissions = SfdcCommon.querySfdcObject(connectionSource, soqlFieldPermissions + " WHERE ParentId = \'" + sourcePermissionSetId + "\'");          
            Map<String, SObject> mapTargetFieldPermissions = SfdcCommon.querySfdcObject(connectionTarget, soqlFieldPermissions + " WHERE ParentId = \'" + targetPermissionSetId + "\'");
            
            Map<String, String> mapTargetObjectNameToId = new HashMap<String, String>();
            for (SObject record : mapTargetFieldPermissions.values()) {
                mapTargetObjectNameToId.put((String) record.getField("Field"), (String) record.getField("Id"));
            }

            ArrayList<SObject> listRecordsToCreateUpdate = new ArrayList<SObject>();
            for (SObject sourceRecord : mapSourceFieldPermissions.values()) {
                SObject sObjectnew = new SObject();
                sObjectnew.setType("FieldPermissions");
                String objectName = (String) sourceRecord.getField("SobjectType");
                if (mapTargetObjectNameToId.containsKey(objectName)) {
                    sObjectnew.setField("Id", mapTargetObjectNameToId.get(objectName));
                }

                sObjectnew.setField("ParentId", targetPermissionSetId);
                sObjectnew.setField("SobjectType", (String) sourceRecord.getField("SobjectType"));
                sObjectnew.setField("Field", (String) sourceRecord.getField("Field"));
                sObjectnew.setField("PermissionsRead", "true".equalsIgnoreCase((String) sourceRecord.getField("PermissionsRead")) ? true : false);
                sObjectnew.setField("PermissionsEdit", "true".equalsIgnoreCase((String) sourceRecord.getField("PermissionsEdit")) ? true : false);
                listRecordsToCreateUpdate.add(sObjectnew);
            }

            // Create/Update the records
            if (listRecordsToCreateUpdate != null && listRecordsToCreateUpdate.size() > 0) {
                System.out.println("Number of Records: " + listRecordsToCreateUpdate.size());
                SfdcCommon.createUpdateSfdcRecords(connectionTarget, listRecordsToCreateUpdate, 50);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    	
    	System.out.println("\n----createUpdateFieldPermissions Ends ---\n");
    }
//------------------------------------------------------------------------------------------------
    /**
     * @Desc:
     * @Param:
     * @Return:
     */
    private void processCopySetupEntityAccess(String sourcePermissionSetId, String targetPermissionSetId) {
        try {
        	
            System.out.println("\n---processCopySetupEntityAccess Starts---\n");
            
            String soqlQuerySetupEntityAccess = "SELECT Id, ParentId, SetupEntityId, SetupEntityType FROM SetupEntityAccess WHERE ParentId = \'" + sourcePermissionSetId + "\'";
            Map<String, SObject> mapSetupEntityAccess = SfdcCommon.querySfdcObject(connectionSource, soqlQuerySetupEntityAccess);

            String soqlQueryClasses = "SELECT Id,Name,NamespacePrefix FROM ApexClass";
            Map<String, SObject> mapSourceClasses = SfdcCommon.querySfdcObject(connectionSource, soqlQueryClasses);


            String soqlQueryPages = "SELECT Id,Name,NamespacePrefix FROM ApexPage";
            Map<String, SObject> mapSourcePages = SfdcCommon.querySfdcObject(connectionSource, soqlQueryPages);

            Set<String> setSourceClasses = new HashSet<String>();
            Set<String> setSourcePages = new HashSet<String>();

            for (SObject record : mapSetupEntityAccess.values()) {
                String setupEntityId = (String) record.getField("SetupEntityId");

                if (mapSourceClasses.containsKey(setupEntityId)) {
                    SObject classRecord = mapSourceClasses.get(setupEntityId);
                    setSourceClasses.add((String) classRecord.getField("NamespacePrefix") + "-" + (String) classRecord.getField("Name"));
                } else if (mapSourcePages.containsKey(setupEntityId)) {
                    SObject pageRecord = mapSourcePages.get(setupEntityId);
                    setSourcePages.add((String) pageRecord.getField("NamespacePrefix") + "-" + (String) pageRecord.getField("Name"));
                }
            }

            Set<String> setTargetClassIds = new HashSet<String>();
            Map<String, SObject> mapTargetClasses = SfdcCommon.querySfdcObject(connectionTarget, soqlQueryClasses);
            for (SObject targetEntity : mapTargetClasses.values()) {
                String entityId = (String) targetEntity.getField("Id");
                String entityName = (String) targetEntity.getField("NamespacePrefix") + "-" + (String) targetEntity.getField("Name");
                if (setSourceClasses.contains(entityName)) {
                    setTargetClassIds.add(entityId);
                }
            }

            if (setTargetClassIds != null && setTargetClassIds.size() > 0) {
                System.out.println("Number of Classes: " + setTargetClassIds.size());
                ArrayList<SObject> listSObjectClasses = createSetupEntityAccessRecord(setTargetClassIds, targetPermissionSetId, "ApexClass");
                SfdcCommon.createSfdcRecords(connectionTarget, listSObjectClasses, 50);
            }

            Set<String> setTargetPagesIds = new HashSet<String>();
            Map<String, SObject> mapTargetPages = SfdcCommon.querySfdcObject(connectionTarget, soqlQueryPages);
            for (SObject targetEntity : mapTargetPages.values()) {
                String entityId = (String) targetEntity.getField("Id");
                String entityName = (String) targetEntity.getField("NamespacePrefix") + "-" + (String) targetEntity.getField("Name");
                if (setSourcePages.contains(entityName)) {
                    setTargetPagesIds.add(entityId);
                }
            }
            if (setTargetPagesIds != null && setTargetPagesIds.size() > 0) {
                System.out.println("Number of pages: " + setTargetPagesIds.size());
                ArrayList<SObject> listSObjectPages = createSetupEntityAccessRecord(setTargetPagesIds, targetPermissionSetId, "ApexPage");
                SfdcCommon.createSfdcRecords(connectionTarget, listSObjectPages, 50);
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        
        System.out.println("\n---processCopySetupEntityAccess Ends---\n");
    }

//------------------------------------------------------------------------------------------------
    /**
     * @Desc:
     * @Param:
     * @Return:
     */
    private ArrayList<SObject> createSetupEntityAccessRecord(Set<String> setEntityIds, String targetPermissionSetId, String entityType) {
        ArrayList<SObject> listSObject = new ArrayList<SObject>();
        for (String entityId : setEntityIds) {
            SObject sObjectNew = new SObject();
            sObjectNew.setType("SetupEntityAccess");
            sObjectNew.setField("ParentId", targetPermissionSetId);
            sObjectNew.setField("SetupEntityId", entityId);
            //sObjectNew.setField("SetupEntityType", entityType);
            listSObject.add(sObjectNew);
            //System.out.println("sObjectnew " + sObjectNew);
        }
        return listSObject;
    }

//------------------------------------------------------------------------------------------------
    /**
     * @Desc:
     * @Param:
     * @Return:
     */
    private ArrayList<SObject> createFieldPermissionsRecords(
        String objectApiName,
        Set<String> setFieldApiName,
        String targetPermissionSetId,
        Boolean permissionsRead,
        Boolean permissionsEdit
    ) {
        ArrayList<SObject> listSObject = new ArrayList<SObject>();
        for (String fieldApiName : setFieldApiName) {
            SObject sObjectNew = new SObject();
            sObjectNew.setType("ObjectPermissions");
            sObjectNew.setField("ParentId", targetPermissionSetId);
            sObjectNew.setField("SobjectType", objectApiName);
            sObjectNew.setField("Field", objectApiName + "." + fieldApiName);
            sObjectNew.setField("PermissionsRead", permissionsRead);;
            sObjectNew.setField("PermissionsEdit", permissionsEdit);
            listSObject.add(sObjectNew);
        }
        return listSObject;
    }

//------------------------------------------------------------------------------------------------
}