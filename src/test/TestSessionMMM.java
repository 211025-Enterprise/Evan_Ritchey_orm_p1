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
        @SaveFieldMMM()
        private boolean testBoolPrivate;
        @SaveFieldMMM()
        public boolean testBoolPublic;
        @SaveFieldMMM()
        private int testIntPrivate;
        @SaveFieldMMM()
        public int testIntPublic;
    }

    @Test
    public void testSessionMMMcreateOneValue(){
        SessionMMM mmm = new SessionMMM();
        mmm.create(new DummyClassType2("Test", 100));
    }

    @Test
    public void testSessionMMMCreateGet(){
        SessionMMM mmm = new SessionMMM();
        mmm.create( new DummyClassType2("Hello", 55));
        mmm.create( new DummyClassType2("GoodBye", 910));
        DummyClassType2 dmct2 = new DummyClassType2("I hope this works", 1453);
        mmm.create(dmct2);

        SessionMMM.FieldValuePair[] fvp = new SessionMMM.FieldValuePair[1];
        fvp[0] = new SessionMMM.FieldValuePair("intTest", 910);
        Object[] retrieved = mmm.get(dmct2,fvp);
        DummyClassType2 retrivedDct2 = (DummyClassType2) retrieved[0];
        System.out.println(retrivedDct2.getStringTest());
    }

    @Test
    public void testUpdateDummyClassType2(){
        SessionMMM mmm = new SessionMMM();

        SessionMMM.FieldValuePair[] values = new SessionMMM.FieldValuePair[1];
        values[0] = new SessionMMM.FieldValuePair("stringTest","Happiness");

        SessionMMM.FieldValuePair[] constraints = new SessionMMM.FieldValuePair[1];
        constraints[0] = new SessionMMM.FieldValuePair("intTest",1453);

        mmm.update(new DummyClassType2("Ignore",45),values,constraints);
    }

    private static class DummyClassType2{
        @SaveFieldMMM()
        private String stringTest;
        @SaveFieldMMM()
        private int intTest;

        public DummyClassType2(){}
        public DummyClassType2(String stringTest,int intTest){
            this.stringTest = stringTest;
            this.intTest = intTest;
        }

        public String getStringTest(){
            return stringTest;
        }
    }
}
