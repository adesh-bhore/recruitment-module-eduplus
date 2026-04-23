package common

import com.google.api.services.directory.model.UserName
import grails.gorm.transactions.Transactional
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.DirectoryScopes;
import com.google.api.services.directory.model.User;
import com.google.api.services.directory.model.Users

@Transactional
class AdminSDKDirectoryQuickstartService {

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "VIERPgsuite";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static  String TOKENS_DIRECTORY_PATH = "tokens";
    private static LocalServerReceiver receiver = null;
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
   def SCOPES =[]

     def CREDENTIALS_FILE_PATH = "";
     def CREDENTIALS_FILE_NAME = "";
     def PORT = "";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    def getCredentials(final NetHttpTransport HTTP_TRANSPORT,org,type)
            {
        // Load client secrets.
//        AWSBucketService awsBucketService =new AWSBucketService()
//        AWSBucket awsBucket = AWSBucket.findByContent("documents")
//        InputStream inputStream=  awsBucketService.downloadContentFromBucket(awsBucket?.bucketname,awsBucket?.region,"https://vierp.s3.ap-south-1.amazonaws.com/cloud/gsuite/credentials_vit/credentialsvit.json")

            CREDENTIALS_FILE_PATH=org?.gsuit_credentials_file_path
            TOKENS_DIRECTORY_PATH=org?.gsuit_credentials_file_path
            CREDENTIALS_FILE_NAME=org?.gsuit_credentials_file_name
                PORT=org?.gsuit_port_number
                java.io.File clientSecretFilePath = new java.io.File(CREDENTIALS_FILE_PATH,CREDENTIALS_FILE_NAME);
                InputStream inputStream = new FileInputStream(clientSecretFilePath);
                if (inputStream == null) {
                    throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
                }
                println(CREDENTIALS_FILE_PATH)
                println(CREDENTIALS_FILE_NAME)
                println(PORT)

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream));
                SCOPES.add(DirectoryScopes.ADMIN_DIRECTORY_USER);
                SCOPES.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP);
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

                if(receiver == null){
                    receiver = new LocalServerReceiver.Builder().setPort(PORT).build();
                }else{
                    receiver.stop();
                    receiver = new LocalServerReceiver.Builder().setPort(PORT).build();
                }

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    def getAllUser(org){

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Directory service =
                new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT,org,"user"))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

        // Print the first 10 users in the domain.
        Users result = service.users().list()
                .setCustomer("my_customer")
                .setMaxResults(10)
                .setOrderBy("email")
                .execute();
        List<User> users = result.getUsers();
        if (users == null || users.size() == 0) {
         println(users)
        } else {
            println("Users:");
            for (User user : users) {
               println(user.getName().getFullName());
            }
        }
    }

    def checkEmailPresent(org,email)
    {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Directory service =
                new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT,org,"user"))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        try{
            def present = service.users().get(email).setProjection("full").execute();
            if(present)
                return true
            else
                return false
        }catch (Exception e)
        {
            println(e)
            return false
        }

    }

    def creategmailaccount(def firstname, def lastname, def email, def password, def org)  {

        try{
        email = email.replaceAll(" ", "")
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Directory service =
                new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT,org,"user"))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

            User user = new User();
            UserName name = new UserName();
            name.setFamilyName(lastname);
            name.setGivenName(firstname);
            user.setName(name);
            user.setPassword(password);
            user.setPrimaryEmail(email);
            user = service.users().insert(user).execute();
            HashMap hashMap=new HashMap()
            hashMap.put("msg","200")
            hashMap.put("user",user)
            return hashMap

        } catch (Exception e) {
            HashMap hashMap=new HashMap()
            hashMap.put("msg",e)
            return hashMap
        }
    }
    def deletegmailaccount(def email,org)  {
        println("email")
        println(email)
        try{
        email = email.replaceAll(" ", "")
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Directory service =
                new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT,org,"user"))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

            def user =service.users().delete(email).execute()

            HashMap hashMap=new HashMap()
            hashMap.put("msg","200")
            return hashMap

        } catch (Exception e) {
            HashMap hashMap=new HashMap()
            hashMap.put("msg",e)
            return hashMap
        }
    }
    def resetemailpassword(def email,def password, def org){
        try{
        email = email.replaceAll(" ", "")
        println("resetemailpassword :: ")
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Directory service = new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, org, 'user'))
                .setApplicationName(APPLICATION_NAME)
                .build();
            def present = service.users().get(email).setProjection("full").execute();
            present.setPassword(password);
            present = service.users().update(email, present).execute();
            HashMap hashMap=new HashMap()
            hashMap.put("msg","200")
            hashMap.put("user",present)
            return hashMap
        } catch (Exception e) {
        HashMap hashMap=new HashMap()
        hashMap.put("msg",e)
        return hashMap
    }
    }
    def blockGmailAccount(def email,org)  {
        println("email")
        println(email)
        try{
            email = email.replaceAll(" ", "")
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Directory service =
                    new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT,org,"user"))
                            .setApplicationName(APPLICATION_NAME)
                            .build();
            def present = service.users().get(email).setProjection("full").execute();
            present.setSuspended(true);
            present = service.users().update(email, present).execute();

            HashMap hashMap=new HashMap()
            hashMap.put("msg","200")
            return hashMap

        } catch (Exception e) {
            HashMap hashMap=new HashMap()
            hashMap.put("msg",e)
            return hashMap
        }
    }
    def unBlockGmailAccount(def email,org)  {
        println("email")
        println(email)
        try{
            email = email.replaceAll(" ", "")
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Directory service =
                    new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT,org,"user"))
                            .setApplicationName(APPLICATION_NAME)
                            .build();

            def present = service.users().get(email).setProjection("full").execute();
            present.setSuspended(false);
            present = service.users().update(email, present).execute();
            HashMap hashMap=new HashMap()
            hashMap.put("msg","200")
            return hashMap

        } catch (Exception e) {
            HashMap hashMap=new HashMap()
            hashMap.put("msg",e)
            return hashMap
        }
    }

}
