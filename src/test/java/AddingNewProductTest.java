import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.*;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AddingNewProductTest {

    static WebDriver driver;
    static WebDriverWait wait;

    String productName;
    String productCode;
    ClassLoader classLoader = getClass().getClassLoader();
    File file;
    String fileAbsolutePath;

    @BeforeClass
    public static void setUp(){
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, 4);

        driver.get("http://localhost/litecart/admin/");
        driver.findElement(By.name("username")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("admin");
        driver.findElement(By.name("login")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success")));

    }

    @AfterClass
    public static void tearDown(){
        driver.quit();
    }

    @Before
    public void prepareProductFields(){
        productName = String.valueOf(System.currentTimeMillis());
        productCode = String.valueOf(System.currentTimeMillis());
        file = new File(classLoader.getResource("image.png").getFile());
        fileAbsolutePath = file.getAbsolutePath();

    }

    @After
    public void clearProducts(){
        Alert deleteConfirmationAlert;

        driver.get("http://localhost/litecart/admin/?app=catalog&doc=catalog");
        driver.findElement(By.xpath("//a[contains(text(), '" + productName + "')]/..")).click();
        driver.findElement(By.name("delete")).click();
        deleteConfirmationAlert = driver.switchTo().alert();
        System.out.println(deleteConfirmationAlert.getText());
        deleteConfirmationAlert.accept();

        // it is required to wait a bit after alert confirmation, otherwise driver quits before product is actually deleted;
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".notice.success")));


    }

    @Test
    public void testAddingTheProduct(){

        driver.findElement(By.cssSelector("[href*='doc=catalog']")).click();
        driver.findElement(By.xpath("//a[contains(text(), 'Add New Product')]")).click();
        driver.findElement(By.cssSelector("[value='1']")).click();
        driver.findElement(By.cssSelector("[name*='name']")).sendKeys(productName);
        driver.findElement(By.name("code")).sendKeys(productCode);
        driver.findElement(By.name("quantity")).clear();
        driver.findElement(By.name("quantity")).sendKeys("10");
        driver.findElement(By.cssSelector("[type=file")).sendKeys(fileAbsolutePath);
        driver.findElement(By.name("save")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(), '" + productName + "')]")));

        driver.get("http://localhost/litecart/en/");
        driver.findElement(By.cssSelector("[title='" + productName + "']")).click();
        driver.findElement(By.name("add_cart_product")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cart")));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.className("quantity"), "1"));

        driver.findElement(By.xpath("//a[contains(text(), 'Checkout')]")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(text(), '" + productName + "')]")));
        driver.findElement(By.name("remove_cart_item")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//td[contains(text(), '" + productName + "')]")));
        assertEquals(0, driver.findElements(By.cssSelector("td.item")).size());

    }
}
