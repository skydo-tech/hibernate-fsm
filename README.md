# Finite State Machine Library

### Event Listeners
Registered by `FSMIntegrator`

1. Pre Insert
2. Post Insert
3. Pre Update
4. Post Update

## Good articles

1. [Event listeners](https://vladmihalcea.com/hibernate-event-listeners/)

## Annotations

### 1. StateMachine
Declared over a field of a class/entity.
E.g.
```
@Entity(name = "exporter")
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
@RequiredArgsConstructor
@FieldNameConstants
public class Exporter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "business_pan")
    private String businessPan;
    @Column(name = "onboarding_state")
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @StateMachine(
        initialState = "INITIATED",
        config = {
            @Transition(fromState = "INITIATED", toState = {"KYC_COMPLETE"}),
            @Transition(fromState = "KYC_COMPLETE", toState = "ONBOARDING_COMPLETE")
        }
    )
    private OnboardingState onboardingState;
}
```
### 2. StateValidatorConfig
### 3. TransitionValidator


