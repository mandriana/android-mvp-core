package com.mandria.android.mvp;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private static final String TEST_VALUE = "testValue";
    private static final String TEST_ERROR = "testError";
    private static final Exception EXCEPTION = new Exception(TEST_ERROR);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private View view;

    @InjectMocks
    private ExampleTestPresenter presenter;

    @Before
    public void setup() {
        presenter = new ExampleTestPresenter();
    }

    @Test
    public void presenterOnNextTest() throws Exception {
        presenter.startFoo().test().performOnNext(view, TEST_VALUE);
        Mockito.verify(view).foo(TEST_VALUE);
    }

    @Test
    public void presenterOnErrorTest() throws Exception {
        presenter.startFoo().test().performOnError(view, EXCEPTION);
        Mockito.verify(view).errorFoo(EXCEPTION.getMessage());
    }

    @Test
    public void presenterOnCompleteTest() throws Exception {
        presenter.startFoo().test().performOnComplete(view);
        //In the presenter, the complete callback is null so nothing happens
        Mockito.verify(view, Mockito.never()).errorFoo(EXCEPTION.getMessage());
        Mockito.verify(view, Mockito.never()).foo(TEST_VALUE);
    }

}