import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import M.Money;


public class Server_X_Client {
    public static void main(String args[]) {

        Socket s=null;
        ServerSocket ss2=null;
        System.out.println("Server Listening......");
        try {
            ss2 = new ServerSocket(4445); // can also use static final PORT_NUM , when defined
        }
        catch(IOException e) {
            e.printStackTrace();
            System.out.println("Server error");
        }

        while(true) {
            try {
                s= ss2.accept();
                System.out.println("connection Established");
                ServerThread st=new ServerThread(s);
                st.start();
            }
            catch(Exception e) {
                e.printStackTrace();
                System.out.println("Connection Error");
            }
        }
    }
}

class ServerThread extends Thread {  
    int opt;
    String client_name=null;
    BufferedReader  is = null;
    PrintWriter os=null;
    Socket s=null;
    public static Money m = new Money(); //static object between multiple threads

    public ServerThread(Socket s) {
        this.s=s;
    }

    public void run() {
        //Accounts login=null;
        String login="";
        try {
            is= new BufferedReader(new InputStreamReader(s.getInputStream()));
            os=new PrintWriter(s.getOutputStream());
        }
        catch(IOException e) {
            System.out.println("IO error in server thread");
        }

        try {
            do {
                opt = Integer.parseInt(is.readLine());
                switch (opt) {
                    case 1 ://accept credentials
                            os.println("Enter username:");
                            os.flush();
                            String username = is.readLine();
                            os.println("Enter password:");
                            os.flush();
                            String password = is.readLine();
                            login = m.login(username,password); //login to account

                            if (login!="") {
                                this.setName(login); //set Thread name to Account name
                                os.println("Welcome "+login+"opt");
                            }
                            else {
                                os.println("Login Failed.opt");
                            }
                            break;

                    case 2 : if (login!="") {
                                //receiving account name
                                os.println("Who do you want to send it to?");
                                os.flush();
                                String receiver = is.readLine();
                                //transfer amount
                                os.println("Enter amount to be sent: ");
                                os.flush();
                                float amt = Float.parseFloat(is.readLine());
                                //Transaction and result
                                int status = m.transaction(login,receiver,amt);
                                if (status==1)
                                    os.println("\u0930 "+amt+" sent to "+receiver+" successfully.opt");
                                else if (status==-1)
                                    os.println("Transaction unsuccessful. Check Balance.opt");
                                else if (status==0)
                                    os.println("No such receiver account exists.opt");
                            }
                            else
                                os.println("Please login!opt");
                            break;
                    
                    case 3 : if (login!="")
                                os.println("Your Balance is "+m.showBalance(login)+"opt");
                            else
                                os.println("Please login!opt");
                            break;

                    case 4 : os.println("Bye!");
                             break;
                    default : os.println("Wrong Choice!opt");
                              
                }
                os.flush();
            } while (opt<4);
        } catch (IOException e) {

            client_name=this.getName();
            System.out.println("IO Error/ Client "+client_name+" terminated abruptly");
        }
        catch(NullPointerException e) {
            client_name=this.getName();
            System.out.println("Client "+client_name+" Closed");
        }

        finally {//closes all open connections
            try {
                System.out.println("Connection Closing..");
                if (is!=null){
                    is.close(); 
                    System.out.println(" Socket Input Stream Closed");
                }

                if(os!=null){
                    os.close();
                    System.out.println("Socket Out Closed");
                }
                
                if (s!=null){
                s.close();
                System.out.println("Socket Closed");
                }
            }
            catch(IOException ie) {
                System.out.println("Socket Close Error");
            }
        }//end finally
    }
}