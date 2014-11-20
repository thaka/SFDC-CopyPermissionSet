Salesforce Permission Set Copy Utility
=================

This java utility copies the permission set from one Salesforce Org (Source Org) into the other (Target Org). This will require the permission set on the Target Org to be created before running this utility.

### TODO:
 1. Download the project
 2. Set the following parameters:
  * sourcePermissionSetId -- The SFDC Id of the Permission Set to be copied from the Source Org
  * targetPermissionSetId -- The SFDC Id of the Permission Set to be updated from the Target Org
  * Source Org parameters "userName", "Password", authEndPoint
  * Target Org parameters "userName", "Password", authEndPoint
  
  
### NOTES:
1. When the object or fields from the Source Org is missing on the Target Org the utility will throw the following the error:
 * Missing object on destination ORG --> ERROR Updating Record: Sobject Type Name: bad value for restricted picklist field: __ObjectName__
 * Missing Field on destination ORG --> ERROR Updating Record: Field Name: bad value for restricted picklist field: __ObjectName.FieldName__
