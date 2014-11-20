package sfdcPermissionSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sforce.soap.partner.*;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.*;


public class SfdcCommon {

//------------------------------------------------------------------------------------------------

    /**
     * @Desc:
     * @Param:
     * @Return:
     */
    public static PartnerConnection doLogin(String username, String password, String authEndPoint) {
        System.out.println("Login on : " + authEndPoint);
        try {
            ConnectorConfig config = new ConnectorConfig();
            config.setUsername(username);
            config.setPassword(password);
            config.setAuthEndpoint(authEndPoint);

            PartnerConnection connection = Connector.newConnection(config);
            printUserInfo(connection, config);

            return connection;
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }

        return null;
    }

//------------------------------------------------------------------------------------------------
    /**
     * @Desc:
     * @Param:
     * @Return:
     */
    public static void doLogout(PartnerConnection connection) {
        try {
            connection.logout();
            System.out.println("Logged out.");
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }

//  ------------------------------------------------------------------------------------------------
    /**
     * @Desc:
     * @Param:
     * @Return:
     */
    public static void printUserInfo(PartnerConnection connection, ConnectorConfig config) {
        try {
            GetUserInfoResult userInfo = connection.getUserInfo();

            System.out.println("\nLogging in ...\n");
            System.out.println("UserID: " + userInfo.getUserId());
            System.out.println("User Full Name: " + userInfo.getUserFullName());
            System.out.println("User Email: " + userInfo.getUserEmail());
            System.out.println();
            System.out.println("SessionID: " + config.getSessionId());
            System.out.println("Auth End Point: " + config.getAuthEndpoint());
            System.out.println("Service End Point: " + config.getServiceEndpoint());
            System.out.println();
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }

//------------------------------------------------------------------------------------------------
    /**
    * @throws ConnectionException
     * @Desc:
    * @Param:
    * @Return:
    */
    public static Map<String, SObject> querySfdcObject(PartnerConnection connection, String soqlQuery) throws ConnectionException {
        Map<String, SObject> mapSObjects = new HashMap<String, SObject>();
        System.out.println("soqlQuery: " + soqlQuery);
        QueryResult queryResults = connection.query(soqlQuery);
        boolean done = false;

        if (queryResults.getSize() > 0) {
            while (!done) {
                //System.out.println(queryResults.getSize());
                for (int i = 0; i < queryResults.getRecords().length; i++) {
                    SObject sObjResult = queryResults.getRecords()[i];
                    String recordId = (String) sObjResult.getField("Id");

                    mapSObjects.put(recordId, sObjResult);
                }

                if (queryResults.isDone()) {
                    done = true;
                } else {
                    queryResults = connection.queryMore(queryResults.getQueryLocator());
                }
            }
        }

        System.out.println("Query Result mapSObjects.size(): " + mapSObjects.size());
        return mapSObjects;
    }

//------------------------------------------------------------------------------------------------
    /**
    * @Desc:
    * @Param:
    * @Return:
    */
    public static void createSfdcRecords(PartnerConnection connectionTarget, ArrayList<SObject> listRecords, int batchSize) {
        // Create/Update the records
        try {
            int nrBatches = listRecords.size() / batchSize + 1;
            for (int x = 0; x < nrBatches; x = x + 1) {
                int fromIndex = batchSize * x;
                int toIndex = batchSize * (x + 1) < listRecords.size() ? batchSize * (x + 1) : listRecords.size();
                System.out.println("Creating/Updating Records: fromIndex = " + fromIndex + " -- toIndex = " + toIndex);
                List<SObject> subListToUpdate = (List<SObject>) listRecords.subList(fromIndex, toIndex);

                SObject[] recordsToUpdate = subListToUpdate.toArray(new SObject[subListToUpdate.size()]);
                SaveResult[] saveResults = connectionTarget.create(recordsToUpdate);
                // check the returned results for any errors
                for (int i = 0; i < saveResults.length; i++) {
                    if (saveResults[i].isSuccess()) {
                        //System.out.println(i + ". Successfully Updated Record - Id: " + saveResults[i].getId());
                    } else {
                        Error[] errors = saveResults[i].getErrors();
                        for (int j = 0; j < errors.length; j++) {
                            System.out.println("ERROR Updating Record: " + errors[j].getMessage());
                        }
                    }
                }
            }
        } catch (ConnectionException e1) {
            e1.printStackTrace();
            //System.out.println(e1.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            //System.out.println(ex.getMessage());
        }
    }

//------------------------------------------------------------------------------------------------
    /**
    * @Desc:
    * @Param:
    * @Return:
    */
    public static void updateSfdcRecords(PartnerConnection connectionTarget, ArrayList<SObject> listRecords, int batchSize) {
        // Create/Update the records
        try {
            int nrBatches = listRecords.size() / batchSize + 1;
            for (int x = 0; x < nrBatches; x = x + 1) {
                int fromIndex = batchSize * x;
                int toIndex = batchSize * (x + 1) < listRecords.size() ? batchSize * (x + 1) : listRecords.size();
                System.out.println("Creating/Updating Records: fromIndex = " + fromIndex + " -- toIndex = " + toIndex);
                List<SObject> subListToUpdate = (List<SObject>) listRecords.subList(fromIndex, toIndex);

                SObject[] recordsToUpdate = subListToUpdate.toArray(new SObject[subListToUpdate.size()]);
                SaveResult[] saveResults = connectionTarget.update(recordsToUpdate);
                // check the returned results for any errors
                for (int i = 0; i < saveResults.length; i++) {
                    if (saveResults[i].isSuccess()) {
                        //System.out.println(i + ". Successfully Updated Record - Id: " + saveResults[i].getId());
                    } else {
                        Error[] errors = saveResults[i].getErrors();
                        for (int j = 0; j < errors.length; j++) {
                            System.out.println("ERROR Updating Record: " + errors[j].getMessage());
                        }
                    }
                }
            }
        } catch (ConnectionException e1) {
            e1.printStackTrace();
            //System.out.println(e1.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            //System.out.println(ex.getMessage());
        }
    }

//------------------------------------------------------------------------------------------------
    /**
    * @Desc:
    * @Param:
    * @Return:
    */
    public static void createUpdateSfdcRecords(PartnerConnection connectionTarget, ArrayList<SObject> listRecords, int batchSize) {
        // Create/Update the records
        try {
            int nrBatches = listRecords.size() / batchSize + 1;
            for (int x = 0; x < nrBatches; x = x + 1) {
                int fromIndex = batchSize * x;
                int toIndex = batchSize * (x + 1) < listRecords.size() ? batchSize * (x + 1) : listRecords.size();
                System.out.println("Creating/Updating Records: fromIndex = " + fromIndex + " -- toIndex = " + toIndex);
                List<SObject> subListToCreateUpdate = (List<SObject>) listRecords.subList(fromIndex, toIndex);

                SObject[] recordsToCreateUpdate = subListToCreateUpdate.toArray(new SObject[subListToCreateUpdate.size()]);
                UpsertResult[] saveResults = connectionTarget.upsert("Id", recordsToCreateUpdate);
                // check the returned results for any errors
                for (int i = 0; i < saveResults.length; i++) {
                    if (saveResults[i].isSuccess()) {
                        //System.out.println(i + ". Successfully Updated Record - Id: " + saveResults[i].getId());
                    } else {
                        Error[] errors = saveResults[i].getErrors();
                        for (int j = 0; j < errors.length; j++) {
                            System.out.println("ERROR Updating Record: " + errors[j].getMessage());
                        }
                    }
                }
            }
        } catch (ConnectionException e1) {
            e1.printStackTrace();
            //System.out.println(e1.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            //System.out.println(ex.getMessage());
        }
    }

//  ------------------------------------------------------------------------------------------------
}
