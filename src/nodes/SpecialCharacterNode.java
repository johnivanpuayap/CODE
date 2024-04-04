package src.nodes;

import java.util.HashSet;
import java.util.Set;
import src.utils.Position;

public class SpecialCharacterNode extends ASTNode {
    private String character;
    private boolean isSpecialCharacter;

    public SpecialCharacterNode(String character, Position position) {
        super(position);
        System.out.println("CHA123:" + character);
        if (!checkIsSpecialCharacter(character)) {
            throw new RuntimeException("Invalid special character: " + character + " at " + position);
        }
        this.character = character;
    }

    public boolean checkIsSpecialCharacter(String character) {
        Set<String> specialCharacters = new HashSet<String>();
        specialCharacters.add("$");
        specialCharacters.add("#");
        specialCharacters.add("[");
        specialCharacters.add("]");

        if(character.equals("[") ||  character.equals("]")) {
            return true;
        } 

        return specialCharacters.contains(character);
    }

    // Getters for value and position
    public String getCharacter() {
        return character;
    }

    public Position getPosition() {
        return super.getPosition();
    }

    public String getValue() {
        if (character == "$") {
            return "\n";
        }
        if (character.equals("#")) {
            return "#";
        }

        if (character.equals("[")) {
            return "[";
        }

        if (character.equals("]")) {
            return "]";
        }

        return null;
    }
}