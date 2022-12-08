## Class and their Responsibilities
### 1. StateValidatorConfig
Stores `entityFieldValidatorMap` and `entityFieldPostUpdateActionMap` in the memory.
These maps are fired up once a application boots up.
To understand the map skeleton, have a look at the code comment of `src/main/java/com/skydo/lib/fsm/config/StateValidatorConfig.java`

## Annotations

### 1. StateMachine
Declared over a field of a class/entity.
E.g.


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
```kotlin
    fun postUpdateHandlerMethod(entityId: Long, oldFieldValue: Any, newFieldValue: Any)
```


This annotation picks up functions and executes **OUTSIDE** of transaction boundary on both of the following events
1. Post Update
2. Post Insert: For post insert `oldValue` will always be empty (`""`) string

