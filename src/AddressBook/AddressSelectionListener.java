package AddressBook;

import org.jetbrains.annotations.Nullable;

public interface AddressSelectionListener {
    void contactSelected(@Nullable ContactInfo contactInfo);
}
