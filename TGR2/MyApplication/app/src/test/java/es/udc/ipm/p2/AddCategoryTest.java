package es.udc.ipm.p2;

import android.content.SharedPreferences;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AddCategoryTest {

    private Map map;

    private Bundle saveInstanceState = null;

    @Mock
    MainActivity mainActivity;

    @Mock
    SharedPreferences mockSharedPreferences;

    @Before
    public void initMocks() {
        map = new HashMap<>();
        mainActivity = createMockActivity();
    }

    @Test
    public void categoryAddTest() {
        Model model = Model.getInstance(mainActivity, saveInstanceState);
        String category1 = "Aaaaaaaaaaa";
        String category2 = "Baaaaaaaaaa";

        model.add(category2);
        assertEquals(model.get(0), category2);

        model.add(category1);
        assertEquals(model.get(0), category1);
        assertEquals(model.get(1), category2);
    }

    private MainActivity createMockActivity() {
        when(mainActivity.getSharedPreferences(anyString(), anyInt()))
                .thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.getAll()).thenReturn(map);

        return mainActivity;
    }
}
