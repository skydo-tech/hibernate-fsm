# Finite State Machine Library

### Event Listeners
Registered by `FSMIntegrator`

1. Pre Insert
2. Post Insert
3. Pre Update
4. Post Update

## Good articles

1. [Event listeners](https://vladmihalcea.com/hibernate-event-listeners/)

## Class and their Responsibilities
### 1. StateValidatorConfig
Stores `entityFieldValidatorMap` and `entityFieldPostUpdateActionMap` in the memory.
These maps are fired up once a application boots up.
To understand the map skeleton, have a look at the code comment of `src/main/java/com/skydo/lib/fsm/config/StateValidatorConfig.java`

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

### 2. TransitionValidatorHandler
Annotated over a class with two variables
1. entity
2. field
Generally the consumer class lists all the validator methods which are executed while `postUpdate` event

### 3. TransitionValidator
Annotated over a method. This validates and makes sure the new state is a valid transition on top of the normal config check.
The purpose of this method is to do external checks which config can't do.

### 4. PostUpdateActionHandler
Annotated over a class with two variables
1. entity
2. field

### 5. PostUpdateAction
 IMPORTANT: function signature which makes use of this annotation must follow a signature described below
 While executing this function from this hibernate layer all these values will be passed.

	 fun postUpdateHandlerMethod(entityId: Long, oldFieldValue: Any, newFieldValue: Any)

This annotation picks up functions and executes **OUTSIDE** of transaction boundary on both of the following events
1. Post Update
2. Post Insert: For post insert `oldValue` will always be empty (`""`) string
