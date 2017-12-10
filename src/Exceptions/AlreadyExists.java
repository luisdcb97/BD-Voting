package Exceptions;

public class AlreadyExists extends Exception {
    public AlreadyExists(){
        super();
    }

    public AlreadyExists(String message){
        super(message);
    }
}

