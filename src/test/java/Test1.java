import com.emyiqing.model.ClassReference;
import com.emyiqing.util.SaveUtil;
import org.junit.Test;

import java.util.List;

public class Test1 {
    @Test
    @SuppressWarnings("unchecked")
    public void test(){
        Object object = SaveUtil.read("classes.dat");
        List<ClassReference> classes = (List<ClassReference>) object;
        System.out.println(classes);
    }
}
