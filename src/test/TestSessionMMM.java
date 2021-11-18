import annotations.SaveFieldMMM;
import org.junit.Test;
import persistence.orm.SessionMMM;

public class TestSessionMMM {

    @Test
    public void testSessionMMMcreateTable(){
        SessionMMM mmm = new SessionMMM();
        mmm.create(new dummyClassType());
    }

    private static class dummyClassType{
        @SaveFieldMMM
        private boolean testBoolPrivate;
        @SaveFieldMMM
        public boolean testBoolPublic;
        @SaveFieldMMM
        private int testIntPrivate;
        @SaveFieldMMM
        public int testIntPublic;
    }
}
