package ca.wilkinsonlab.sadi.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.wilkinsonlab.sadi.utils.OwlUtils;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class OntologyTest
{
	private static final Logger log = Logger.getLogger( OntologyTest.class );

	@SuppressWarnings("serial")
	public static void main(String[] args)
	{
		Map<OntModelSpec, String> specNames = new HashMap<OntModelSpec,String>() {{
			put(OntModelSpec.OWL_DL_MEM_RULE_INF, "OWL_DL_MEM_RULE_INF");
//			put(OntModelSpec.OWL_MEM_TRANS_INF, "OWL_MEM_TRANS_INF");
//			put(OntModelSpec.OWL_MEM_RDFS_INF, "OWL_MEM_RDFS_INF");
//			put(OntModelSpec.OWL_MEM_RULE_INF, "OWL_MEM_RULE_INF");
			put(OntModelSpec.OWL_MEM_MINI_RULE_INF, "OWL_MEM_MINI_RULE_INF");
			put(OntModelSpec.OWL_MEM_MICRO_RULE_INF, "OWL_MEM_MICRO_RULE_INF");
		}};
		
		for (OntModelSpec spec: specNames.keySet()) {
			log.info("running tests for OntModelSpec." + specNames.get(spec));
			OntModel ontModel = ModelFactory.createOntologyModel(spec);

			// comment out these lines if you want to skip running certain ontology tests
			OntModelTest tests[] = new OntModelTest[] { 
					new PropertyRangeTest(ontModel), 
					new InversePropertiesTest(ontModel),
					new EquivalentPropertiesTest(ontModel),
					new TransitiveEquivalentPropertiesTest(ontModel),
					new MultipleEquivalentPropertiesTest(ontModel)
			};
			
			for(OntModelTest test : tests) {
				log.info("running " + test);
				if(test.runTest()) {
					log.info(test + ": PASSED");
				} else {
					log.info(test + ": FAILED");
				}
			}

		}
	}
	
	abstract public static class OntModelTest {
		
		OntModel ontModel;
		String testName;
		
		public OntModelTest(OntModel ontModel, String testName) {
			setOntModel(ontModel);
			setTestName(testName);
		}

		abstract public boolean runTest();

		public OntModel getOntModel() {
			return ontModel;
		}
		public void setOntModel(OntModel ontModel) {
			this.ontModel = ontModel;
		}
		public String getTestName() {
			return testName;
		}
		public void setTestName(String testName) {
			this.testName = testName;
		}
		public String toString() {
			return getTestName();
		}
	}
	
	public static class InversePropertiesTest extends OntModelTest {
		
		public InversePropertiesTest(OntModel ontModel) { 
			super(ontModel, "inverse properties test"); 
		}

		public boolean runTest() {
			OntProperty belongsToPathway = OwlUtils.getOntPropertyWithLoad(getOntModel(), "http://sadiframework.org/ontologies/predicates.owl#belongsToPathway");
			OntProperty inverseProperty = belongsToPathway.getInverse();
			return inverseProperty != null;
		}
	}
	
	public static class PropertyRangeTest extends OntModelTest {
		
		public PropertyRangeTest(OntModel ontModel) { 
			super(ontModel, "property range test"); 
		}

		public boolean runTest() {
			OntProperty p = OwlUtils.getOntPropertyWithLoad(getOntModel(), "http://sadiframework.org/ontologies/predicates.owl#has3DStructure");
			return p.getRange().getURI().equals("http://purl.oclc.org/SADI/LSRN/PDB_Record");
		}
	}	
	
	public static class EquivalentPropertiesTest extends OntModelTest {

		public EquivalentPropertiesTest(OntModel ontModel) { 
			super(ontModel, "equivalent properties test"); 
		}

		public boolean runTest() {
			
			OntModel model = getOntModel();
			
			OntProperty a = model.createOntProperty("a");
			OntProperty b = model.createOntProperty("b");
			
			a.setEquivalentProperty(b);

			boolean hasB = false;
			for(Iterator<? extends OntProperty> i = a.listEquivalentProperties(); i.hasNext(); ) {
				OntProperty synonym = i.next();
				if(synonym.equals(b)) {
					hasB = true;
				}
			}
			
			return hasB;
		}
	}
	
	public static class TransitiveEquivalentPropertiesTest extends OntModelTest {

		public TransitiveEquivalentPropertiesTest(OntModel ontModel) { 
			super(ontModel, "transitive equivalent properties test"); 
		}

		public boolean runTest() {
			
			OntModel model = getOntModel();
			
			OntProperty a = model.createOntProperty("a");
			OntProperty b = model.createOntProperty("b");
			OntProperty c = model.createOntProperty("c");
			
			a.setEquivalentProperty(b);
			b.setEquivalentProperty(c);
			
			boolean hasC = false;
			for(Iterator<? extends OntProperty> i = a.listEquivalentProperties(); i.hasNext(); ) {
				OntProperty synonym = i.next();
				if(synonym.equals(c)) {
					hasC = true;
				}
			}
			
			return hasC;
		}
	}
	
	public static class MultipleEquivalentPropertiesTest extends OntModelTest {

		public MultipleEquivalentPropertiesTest(OntModel ontModel) { 
			super(ontModel, "multiple equivalent properties test"); 
		}

		public boolean runTest() {
			
			OntModel model = getOntModel();
			
			OntProperty a = model.createOntProperty("a");
			OntProperty b = model.createOntProperty("b");
			OntProperty c = model.createOntProperty("c");
			
			a.addEquivalentProperty(b);
			a.addEquivalentProperty(c);

			boolean hasB = false;
			boolean hasC = false;
			
			for(Iterator<? extends OntProperty> i = a.listEquivalentProperties(); i.hasNext(); ) {
				OntProperty synonym = i.next();
				if(synonym.equals(b)) {
					hasB = true;
				} else if(synonym.equals(c)) {
					hasC = true;
				}
			}
			
			return (hasB && hasC);
		}
	}

	
}
