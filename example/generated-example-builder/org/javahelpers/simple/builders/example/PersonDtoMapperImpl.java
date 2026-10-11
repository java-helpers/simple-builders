package org.javahelpers.simple.builders.example;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor"
)
public class PersonDtoMapperImpl implements PersonDtoMapper {

    @Override
    public PersonDto copy(PersonDto source) {
        if ( source == null ) {
            return null;
        }

        PersonDtoBuilder personDto = PersonDtoBuilder.create();

        personDto.birthdate( source.getBirthdate() );
        personDto.mannschaft( source.getMannschaft() );
        personDto.name( source.getName() );
        List<String> list = source.getNickNames();
        if ( list != null ) {
            personDto.nickNames( new ArrayList<String>( list ) );
        }

        return personDto.build();
    }
}
