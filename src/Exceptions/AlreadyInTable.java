package Exceptions;

public class AlreadyInTable extends Exception{
    public AlreadyInTable(){
        super();
    }

    public AlreadyInTable(String message){
        super(message);
    }
}
