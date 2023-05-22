package demoapp.dom.services.core.wrapperFactory;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;

import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;

//tag::class[]
@Named("demo.DemoEntityFactory")
@DomainService(
        nature = NatureOfService.VIEW                               // <.>
)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PrimeNumberGenerator {

    final PrimeNumberFactory<?> primeNumberFactory;
    final WrapperFactory wrapperFactory;

    @Action
    @ActionLayout(hidden = Where.EVERYWHERE)                        // <.>
    public void calculatePrimeNumbersAsync(int from, int upTo) {
        int nextPrime = nextPrime(from);
        if (nextPrime <= upTo) {
            primeNumberFactory.newPrimeNumber(nextPrime);
            wrapperFactory.asyncWrap(
                    this,
                    AsyncControl.returningVoid().withSkipRules()
            ).calculatePrimeNumbersAsync(nextPrime, upTo);          // <.>
        }
    }

    @SneakyThrows
    private static int nextPrime(int number) {
        Thread.sleep(200);                                          // <.>
        number++;
        while (!isPrime(number)) {
            number++;
        }
        return number;
    }

    private static boolean isPrime(int number) {
        //...
//end::class[]
        if (number <= 1) {
            return false;
        }

        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) {
                return false;
            }
        }

        return true;
//tag::class[]
    }
}
//end::class[]
