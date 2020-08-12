package ly.iterative.itly

import ly.iterative.itly.core.Options

interface Plugin {
    fun id(): String
    fun load(options: Options)

    // Tracking methods
    fun alias(userId: String, previousId: String? = null)
    fun identify(userId: String?, properties: Properties? = null)
    fun group(userId: String?, groupId: String, properties: Properties? = null)
    fun track(userId: String?, event: Event)
    fun reset()

    // Validation methods
    fun validate(event: Event): ValidationResponse
    fun onValidationError(validation: ValidationResponse, event: Event)

    fun flush()
    fun shutdown()
}

//export interface Plugin {
//    id(): string;
//
//    // Tracking methods
//    load(options: Options): void;
//    alias(userId: string, previousId: string | undefined): void;
//    identify(userId: string | undefined, properties: Properties | undefined): void;
//    group(
//    userId: string | undefined,
//    groupId: string,
//    properties?: Properties | undefined
//    ): void;
//    page(
//    userId: string | undefined,
//    category: string | undefined,
//    name: string | undefined,
//    properties: Properties | undefined
//    ): void;
//    track(userId: string | undefined, event: Event): void;
//    reset(): void;
//
//    // Validation methods
//    validate(event: Event): ValidationResponse;
//    validationError(validation: ValidationResponse, event: Event): void;
//}
