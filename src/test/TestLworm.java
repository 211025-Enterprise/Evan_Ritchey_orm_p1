import annotations.SaveFieldLworm;
import org.junit.Ignore;
import org.junit.Test;
import persistence.orm.Lworm;

public class TestLworm {

    @Ignore
    @Test
    public void testSessionMMMcreateTable(){
        Lworm mmm = new Lworm();
        mmm.create(new dummyClassType());
    }

    private static class dummyClassType{
        @SaveFieldLworm()
        private boolean testBoolPrivate;
        @SaveFieldLworm()
        public boolean testBoolPublic;
        @SaveFieldLworm()
        private int testIntPrivate;
        @SaveFieldLworm()
        public int testIntPublic;
    }

    @Test
    public void testSessionMMMcreateOneValue(){
        Lworm mmm = new Lworm();
        mmm.create(new DummyClassType2("Test", 100));
    }

    @Test
    public void testSessionMMMCreateGet(){
        Lworm mmm = new Lworm();
        mmm.create( new DummyClassType2("Hello", 55));
        mmm.create( new DummyClassType2("GoodBye", 910));
        DummyClassType2 dmct2 = new DummyClassType2("I hope this works", 1453);
        mmm.create(dmct2);

        Lworm.FieldValuePair[] fvp = new Lworm.FieldValuePair[1];
        fvp[0] = new Lworm.FieldValuePair("intTest", 910);
        Object[] retrieved = mmm.get(dmct2,fvp);
        DummyClassType2 retrivedDct2 = (DummyClassType2) retrieved[0];
        System.out.println(retrivedDct2.getStringTest());
    }

    @Test
    public void testUpdateDummyClassType2(){
        Lworm mmm = new Lworm();

        Lworm.FieldValuePair[] values = new Lworm.FieldValuePair[1];
        values[0] = new Lworm.FieldValuePair("stringTest","Happiness");

        Lworm.FieldValuePair[] constraints = new Lworm.FieldValuePair[1];
        constraints[0] = new Lworm.FieldValuePair("intTest",1453);

        mmm.update(new DummyClassType2("Ignore",45),values,constraints);
    }

    @Test
    public void testDeleteFromDummyClassType2(){
        Lworm lworm = new Lworm();

        DummyClassType2 dummyClassType2 = new DummyClassType2();

        Lworm.FieldValuePair[] values = new Lworm.FieldValuePair[1];
//        values[0] = new Lworm.FieldValuePair("stringTest","Happiness");
        values[0] = new Lworm.FieldValuePair("intTest",55);

        lworm.delete(dummyClassType2,values);
    }

    private static class DummyClassType2{
        @SaveFieldLworm()
        private String stringTest;
        @SaveFieldLworm()
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
