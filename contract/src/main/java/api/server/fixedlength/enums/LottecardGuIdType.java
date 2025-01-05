package api.server.fixedlength.enums;

import api.server.fixedlength.helper.FixedLengthHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
public enum LottecardGuIdType {

    UI {
        @Override
        public String generate(String employeeId, String serialNumber) {
            return String.format("%sUIF%s%s", FixedLengthHelper.getCurrentSystemTime(), FixedLengthHelper.padLeft(employeeId, 7), FixedLengthHelper.padLeft(serialNumber, 7));
        }
    },
    FW {
        @Override
        public String generate(String systemName, String messageTimestamp, String serialNumber) {
            return String.format("%s%s%s%s", FixedLengthHelper.getCurrentSystemDate(), systemName, messageTimestamp, FixedLengthHelper.padLeft(serialNumber, 5));
        }
    },
    FEP {
        @Override
        public String generate(String systemName, String messageTimestamp, String serialNumber) {
            return String.format("%s%s%s%s", FixedLengthHelper.getCurrentSystemDate(), systemName, messageTimestamp, FixedLengthHelper.padLeft(serialNumber, 5));
        }
    },
    ARS {
        @Override
        public String generateWithSuffix(String systemName, String messageTimestamp) {
            return String.format("%s%s%sARSID", FixedLengthHelper.getCurrentSystemDate(), systemName, messageTimestamp);
        }
    },
    VOT {
        @Override
        public String generateWithSuffix(String systemName, String messageTimestamp) {
            return String.format("%s%s%sVOTID", FixedLengthHelper.getCurrentSystemDate(), systemName, messageTimestamp);
        }
    };

    public static final String GENERATE_ERROR = "This method is not supported for this type";

    public String generate(String employeeId, String serialNumber) {
        throw new UnsupportedOperationException(GENERATE_ERROR);
    }

    public String generate(String systemName, String messageTimestamp, String serialNumber) {
        throw new UnsupportedOperationException(GENERATE_ERROR);
    }

    public String generateWithSuffix(String systemName, String messageTimestamp) {
        throw new UnsupportedOperationException(GENERATE_ERROR);
    }

}
